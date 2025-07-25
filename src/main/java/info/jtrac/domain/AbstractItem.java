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

import info.jtrac.util.DateUtils;
import jakarta.persistence.CascadeType;
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

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static jakarta.persistence.InheritanceType.TABLE_PER_CLASS;

@Data
@Entity
@Inheritance(strategy = TABLE_PER_CLASS)
public abstract class AbstractItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    protected long id;

    @Version
    protected int version;

    @ManyToOne
    private Item parent; // slightly different meaning for Item and History

    private String summary;
    private String detail;

    @ManyToOne
    private User loggedBy;

    @ManyToOne
    private User assignedTo;

    private Date timeStamp;
    private Double plannedEffort;
    //===========================
    private Integer status;
    private Integer severity;
    private Integer priority;
    private Integer cusInt01;
    private Integer cusInt02;
    private Integer cusInt03;
    private Integer cusInt04;
    private Integer cusInt05;
    private Integer cusInt06;
    private Integer cusInt07;
    private Integer cusInt08;
    private Integer cusInt09;
    private Integer cusInt10;
    private Double cusDbl01;
    private Double cusDbl02;
    private Double cusDbl03;
    private String cusStr01;
    private String cusStr02;
    private String cusStr03;
    private String cusStr04;
    private String cusStr05;
    private Date cusTim01;
    private Date cusTim02;
    private Date cusTim03;
    private String cusText01;
    private String cusText02;
    private String cusText03;

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

    // we could have used reflection or a Map but doing this way for performance
    public Object getValue(Field.Name fieldName) {
        switch (fieldName) {
            case SEVERITY:
                return severity;
            case PRIORITY:
                return priority;
            case CUS_INT_01:
                return cusInt01;
            case CUS_INT_02:
                return cusInt02;
            case CUS_INT_03:
                return cusInt03;
            case CUS_INT_04:
                return cusInt04;
            case CUS_INT_05:
                return cusInt05;
            case CUS_INT_06:
                return cusInt06;
            case CUS_INT_07:
                return cusInt07;
            case CUS_INT_08:
                return cusInt08;
            case CUS_INT_09:
                return cusInt09;
            case CUS_INT_10:
                return cusInt10;
            case CUS_DBL_01:
                return cusDbl01;
            case CUS_DBL_02:
                return cusDbl02;
            case CUS_DBL_03:
                return cusDbl03;
            case CUS_STR_01:
                return cusStr01;
            case CUS_STR_02:
                return cusStr02;
            case CUS_STR_03:
                return cusStr03;
            case CUS_STR_04:
                return cusStr04;
            case CUS_STR_05:
                return cusStr05;
            case CUS_TIM_01:
                return cusTim01;
            case CUS_TIM_02:
                return cusTim02;
            case CUS_TIM_03:
                return cusTim03;
            case CUS_TEXT_01:
                return cusText01;
            case CUS_TEXT_02:
                return cusText02;
            case CUS_TEXT_03:
                return cusText03;
            default:
                return null; // this should never happen
        }
    }

    // we could have used reflection or a Map but doing this way for performance
    public void setValue(Field.Name fieldName, Object value) {
        switch (fieldName) {
            case SEVERITY:
                severity = (Integer) value;
                break;
            case PRIORITY:
                priority = (Integer) value;
                break;
            case CUS_INT_01:
                cusInt01 = (Integer) value;
                break;
            case CUS_INT_02:
                cusInt02 = (Integer) value;
                break;
            case CUS_INT_03:
                cusInt03 = (Integer) value;
                break;
            case CUS_INT_04:
                cusInt04 = (Integer) value;
                break;
            case CUS_INT_05:
                cusInt05 = (Integer) value;
                break;
            case CUS_INT_06:
                cusInt06 = (Integer) value;
                break;
            case CUS_INT_07:
                cusInt07 = (Integer) value;
                break;
            case CUS_INT_08:
                cusInt08 = (Integer) value;
                break;
            case CUS_INT_09:
                cusInt09 = (Integer) value;
                break;
            case CUS_INT_10:
                cusInt10 = (Integer) value;
                break;
            case CUS_DBL_01:
                cusDbl01 = (Double) value;
                break;
            case CUS_DBL_02:
                cusDbl02 = (Double) value;
                break;
            case CUS_DBL_03:
                cusDbl03 = (Double) value;
                break;
            case CUS_STR_01:
                cusStr01 = (String) value;
                break;
            case CUS_STR_02:
                cusStr02 = (String) value;
                break;
            case CUS_STR_03:
                cusStr03 = (String) value;
                break;
            case CUS_STR_04:
                cusStr04 = (String) value;
                break;
            case CUS_STR_05:
                cusStr05 = (String) value;
                break;
            case CUS_TIM_01:
                cusTim01 = (Date) value;
                break;
            case CUS_TIM_02:
                cusTim02 = (Date) value;
                break;
            case CUS_TIM_03:
                cusTim03 = (Date) value;
            case CUS_TEXT_01:
                cusText01 = (String) value;
                break;
            case CUS_TEXT_02:
                cusText02 = (String) value;
                break;
            case CUS_TEXT_03:
                cusText03 = (String) value;
                break;

            default: // this should never happen
        }
    }

    // must override, History behaves differently from Item
    public abstract Space getSpace();

    public abstract String getRefId();

    public String getCustomValue(Field.Name fieldName) {
        // using accessor for space, getSpace() is overridden in subclass History
        if (fieldName.getType() <= 3) {
            return Optional.ofNullable(getSpace())
                    .map(Space::getMetadata)
                    .map(meta -> meta.getCustomValue(fieldName, (Integer) getValue(fieldName)))
                    .orElse(null); // или значение по умолчанию

        } else {
            Object o = getValue(fieldName);
            if (o == null) {
                return "";
            }
            if (o instanceof Date) {
                return DateUtils.format((Date) o);
            }
            return o.toString();
        }
    }

    public String getStatusValue() {
        // using accessor for space, getSpace() is overridden in subclass History
        return Optional.ofNullable(getSpace())
                .map(Space::getMetadata)
                .map(meta -> meta.getStatusValue(status))
                .orElse(null); // или значение по умолчанию

    }


    public void fetchDefaultValue(Item item) {
        Map<Field.Name, Field> fieldsValue = item.getSpace().getMetadata().getFields();

        for (Field.Name fieldName : fieldsValue.keySet()) {
            setValue(fieldName, item.getValue(fieldName));
        }
    }

}
