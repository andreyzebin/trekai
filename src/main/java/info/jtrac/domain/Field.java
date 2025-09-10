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

import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.Transient;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <code>Metadata</code> is composited of Field elements
 * that represent each of the custom fields that may be
 * used within an item
 */
@NoArgsConstructor
@Data
public class Field implements Serializable {
    private FieldType fieldType;
    private String label;
    private String code;
    private boolean optional;
    private Map<String, String> options;
    private String defaultValue;
    private List<String> optionsOrder;

    @Transient
    public String getRenderValue(String val) {
        if (Objects.requireNonNull(fieldType) == FieldType.SELECT ||
                (options != null && !options.isEmpty())) {
            return options.get(val);
        }
        return val;
    }
}
