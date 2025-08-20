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

import info.jtrac.web.api.dto.FieldUpdateDto;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
@Entity
@Table(name = "history")
public class History extends AbstractItem {

    private Integer type;
    @Column(columnDefinition = "TEXT")
    private String comment;
    private Double actualEffort;

    @ManyToOne
    private Attachment attachment;

    @Embedded
    private FieldUpdateDto change;

    public History() {
        // zero arg constructor
    }

    /**
     * this is used a) when creating snapshot of item when inserting history
     * and b) to create snapshot of item when editing item in which case
     * the status, loggedBy and assignedTo fields are additionally tweaked
     */
    public History(Item item) {
        setStatus(item.getStatus());
        setSummary(item.getSummary());
        setDetail(item.getDetail());
        setLoggedBy(item.getLoggedBy());
        setAssignedTo(item.getAssignedTo());
        // setTimeStamp(item.getTimeStamp());
        setPlannedEffort(item.getPlannedEffort());
        //==========================
    }

    @Override
    public String getRefId() {
        return getParent().getRefId();
    }

    @Override
    public Space getSpace() {
        return getParent().getSpace();
    }

    public int getIndex() {
        int index = 0;
        for (History h : getParent().getHistory()) {
            if (getId() == h.getId()) {
                return index;
            }
            index++;
        }
        return -1;
    }

}
