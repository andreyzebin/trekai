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

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


@Data
@Entity
@Table(name = "metadata")
public class Metadata implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    private int version;

    private Integer type;
    private String name;
    private String description;

    @ManyToOne
    private Metadata parent;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = FieldsConverter.class)
    private Map<String, Field> fields;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = ListToJsonConverter.class)
    private List<String> fieldOrder;

    @Transient
    private Map<String, Role> roles;


    public Metadata() {
        init();
    }

    @Transient
    public List<Field> getOrderedFields() {
        if (fieldOrder == null || fields == null) {
            return List.of(); // или Collections.emptyList()
        }

        return fieldOrder.stream()
                .map(fields::get)
                .filter(Objects::nonNull)
                .toList();
    }


    private void init() {
        fields = new HashMap<>();
        roles = new HashMap<>();
        fieldOrder = new LinkedList<>();
    }

    public void add(Field field) {
        fields.put(field.getCode(), field); // will overwrite if exists
        if (!fieldOrder.contains(field.getCode())) { // but for List, need to check
            fieldOrder.add(field.getCode());
        }
    }

    public void addRole(String roleName) {
        Role role = new Role(roleName);
        roles.put(role.getName(), role);
    }

    public void renameRole(String oldRole, String newRole) {
        // important! this has to be combined with a database update
        Role role = roles.get(oldRole);
        if (role == null) {
            return; // TODO improve JtracTest and assert not null here
        }
        role.setName(newRole);
        roles.remove(oldRole);
        roles.put(newRole, role);
    }

    public void removeRole(String roleName) {
        // important! this has to be combined with a database update
        roles.remove(roleName);
    }


    public int getRoleCount() {
        return roles.size();
    }

    public int getFieldCount() {
        return getFields().size();
    }


    // returning map ideal for JSTL
    public Map<String, Boolean> getRolesAbleToTransition(int fromStatus, int toStatus) {
        Map<String, Boolean> map = new HashMap<String, Boolean>(roles.size());
        for (Role role : roles.values()) {
            State s = role.getStates().get(fromStatus);
            if (s.getTransitions().contains(toStatus)) {
                map.put(role.getName(), true);
            }
        }
        return map;
    }

    public Set<String> getRolesAbleToTransitionFrom(int state) {
        Set<String> set = new HashSet<String>(roles.size());
        for (Role role : roles.values()) {
            State s = role.getStates().get(state);
            if (s.getTransitions().size() > 0) {
                set.add(role.getName());
            }
        }
        return set;
    }

    private State getRoleState(String roleKey, int stateKey) {
        Role role = roles.get(roleKey);
        return role.getStates().get(stateKey);
    }


    public Collection<Role> getRoleList() {
        return roles.values();
    }

    public Collection<String> getRoleKeys() {
        return roles.keySet();
    }

    // introducing Admin permissions per space, slight hack
    // so Role stands for "workflow" role from now on
    public List<String> getAdminRoleKeys() {
        return Arrays.asList(new String[]{Role.ROLE_ADMIN});
    }

    public List<String> getAllRoleKeys() {
        List<String> list = new ArrayList<String>(getRoleKeys());
        list.addAll(getAdminRoleKeys());
        return list;
    }

}
