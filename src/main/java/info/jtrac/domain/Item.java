/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.jtrac.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
@Entity
@Table(name = "items")
public class Item extends AbstractItem {

    private Integer type;

    @ManyToOne
    private Space space;

    private long sequenceNum;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = MapToJsonConverter.class)
    private Map<String, String> fieldValues;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<History> history;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Item> children;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Attachment> attachments;

    @Transient
    private String editReason;

    @Override
    public String getRefId() {
        return Optional.ofNullable(getSpace())
                .map(Space::getPrefixCode)
                .map(prefix -> prefix + "-" + id)
                .orElse("UNKNOWN-" + id);
    }

    public void setValue(String code, Object field) {
        if (this.fieldValues == null) {
            this.fieldValues = new HashMap<>();
        }
        this.fieldValues.put(code, Optional.ofNullable(field).map(Objects::toString).orElse(null));
    }

    public String getValue(String code) {
        if (this.fieldValues == null) {
            this.fieldValues = new HashMap<>();
        }
        return this.fieldValues.get(code);
    }

    public List<History> getHistoryPage() {
        return getHistory() != null
                ? getHistory().stream()
                .sorted(Comparator.comparing(AbstractItem::getTimeStamp))
                .collect(Collectors.toList())
                : Collections.emptyList();
    }

    public String getRender(String code) {
        String value = getValue(code);
        if (value == null) {
            return null;
        }

        return space.getMetadata().getFields().get(code).getRenderValue(value);
    }


    public void add(History h) {
        if (this.history == null) {
            this.history = new LinkedHashSet<History>();
        }
        h.setParent(this);
        this.history.add(h);
    }

    public void add(Attachment attachment) {
        if (attachments == null) {
            attachments = new LinkedHashSet<Attachment>();
        }
        attachments.add(attachment);
    }

    public void addRelated(Item relatedItem, int relationType) {
        if (getRelatedItems() == null) {
            setRelatedItems(new LinkedHashSet<ItemItem>());
        }
        ItemItem itemItem = new ItemItem(this, relatedItem, relationType);
        getRelatedItems().add(itemItem);
    }

    public History getLatestHistory() {
        if (history == null) {
            return null;
        }
        History out = null;
        for (History h : history) {
            out = h;
        }
        return out;
    }

}
