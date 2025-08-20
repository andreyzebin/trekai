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
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static jakarta.persistence.InheritanceType.TABLE_PER_CLASS;

@Data
@Entity
@Inheritance(strategy = TABLE_PER_CLASS)
public abstract class AbstractItem implements Serializable {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    protected long id;

    @Version
    protected int version;

    @ManyToOne
    private Item parent; // slightly different meaning for Item and History

    private String summary;
    @Column(columnDefinition = "TEXT")
    private String detail;

    @ManyToOne
    private User loggedBy;

    @ManyToOne
    private User assignedTo;

    @EqualsAndHashCode.Include
    private Date timeStamp;
    private Double plannedEffort;
    //===========================
    private Integer status;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private Set<ItemUser> itemUsers;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private Set<ItemItem> relatedItems;

    @OneToMany(mappedBy = "relatedItem", cascade = CascadeType.ALL)
    private Set<ItemItem> relatingItems;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private Set<ItemTag> itemTags;

    @Transient
    private boolean sendNotifications = true;


    // must override, History behaves differently from Item
    public abstract Space getSpace();

    public abstract String getRefId();

    public String getStatusValue() {
        // using accessor for space, getSpace() is overridden in subclass History
        return Optional.ofNullable(getSpace())
                .map(Space::getMetadata)
                .map(meta -> meta.getStatusValue(status))
                .orElse(null); // или значение по умолчанию

    }


}
