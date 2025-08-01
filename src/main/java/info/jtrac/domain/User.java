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

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Entity
@Table(name = "users")
public class User implements UserDetails, Serializable, Comparable<User> {

    public static final int SEARCH_NAME = 0;
    public static final int SEARCH_LOGIN_NAME = 1;
    public static final int SEARCH_EMAIL = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Integer type;

    @ManyToOne
    private User parent;

    @Column(unique = true)
    private String loginName;
    private String name;
    @jakarta.persistence.Column(length = 60)
    private String password;
    private String email;
    private String avatarUrl;

    @ManyToOne
    private Metadata metadata;

    private String locale;
    private boolean locked;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<UserSpaceRole> userSpaceRoles = new HashSet<UserSpaceRole>();

    //=============================================================

    public void addSpaceWithRole(Space space, String roleKey) {
        userSpaceRoles.add(new UserSpaceRole(this, space, roleKey));
    }

    public void removeSpaceWithRole(Space space, String roleKey) {
        userSpaceRoles.remove(new UserSpaceRole(this, space, roleKey));
    }

    /**
     * used because there is a rare chance that after a principal "refresh"
     * like after editing spaces for self, on saving profile for self,
     * the "fake" roles like "ROLE_GUEST" will get saved to the database
     * this method has to be called before saving a user object
     */
    public void clearNonPersistentRoles() {
        List<UserSpaceRole> toRemove = new ArrayList<UserSpaceRole>();
        for (UserSpaceRole usr : userSpaceRoles) {
            if (usr.isGuest()) {
                toRemove.add(usr);
            }
        }
        userSpaceRoles.removeAll(toRemove);
    }

    /**
     * when the passed space is null this has a special significance
     * it will return roles that are 'global'
     */
    public List<String> getRoleKeys(Space space) {
        List<String> roleKeys = new ArrayList<String>();
        for (UserSpaceRole usr : userSpaceRoles) {
            Space s = usr.getSpace();
            if (s == space || (s != null && s.equals(space))) {
                roleKeys.add(usr.getRoleKey());
            }
        }
        return roleKeys;
    }


    public Set<Space> getSpaces() {
        Set<Space> spaces = new HashSet<Space>(userSpaceRoles.size());
        for (UserSpaceRole usr : userSpaceRoles) {
            if (usr.getSpace() != null) {
                spaces.add(usr.getSpace());
            }
        }
        return spaces;
    }

    public boolean isAllocatedToSpace(long spaceId) {
        for (UserSpaceRole usr : userSpaceRoles) {
            if (usr.getSpace() != null && usr.getSpace().getId() == spaceId) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdminForSpace(long spaceId) {
        for (UserSpaceRole usr : userSpaceRoles) {
            if (usr.isSpaceAdmin() && usr.getSpace().getId() == spaceId) {
                return true;
            }
        }
        return false;
    }

    public int getSpaceCount() {
        return getSpaces().size();
    }

    public boolean isSuperUser() {
        return getRoleKeys(null).contains(Role.ROLE_ADMIN);
    }

    /**
     * this returns 'valid' spaceRoles, where space not null and role not ROLE_ADMIN
     * also sort by Space name for showing on the dashboard
     * TODO multiple roles per space
     */
    public Collection<UserSpaceRole> getSpaceRoles() {
        Map<String, UserSpaceRole> map = new TreeMap<String, UserSpaceRole>();
        for (UserSpaceRole usr : userSpaceRoles) {
            if (!usr.isAdmin()) {
                map.put(usr.getSpace().getName(), usr);
            }
        }
        return map.values();
    }

    /**
     * returns a sorted map of spaces for this user where the value is
     * a List of role keys, useful for UI display of this
     * users allocated spaces and roles
     */
    public Map<Long, List<UserSpaceRole>> getSpaceRolesMap() {
        Map<Long, List<UserSpaceRole>> map = new TreeMap<Long, List<UserSpaceRole>>();
        for (UserSpaceRole usr : userSpaceRoles) {
            long spaceId = 0;
            if (usr.getSpace() != null) {
                spaceId = usr.getSpace().getId();
            }
            List<UserSpaceRole> list = map.get(spaceId);
            if (list == null) {
                list = new ArrayList<UserSpaceRole>();
                map.put(spaceId, list);
            }
            list.add(usr);
        }
        return map;
    }

    public boolean isGuestForSpace(Space space) {
        if (id == 0) {
            return true;
        }
        for (UserSpaceRole usr : getUserSpaceRolesBySpaceId(space.getId())) {
            if (usr.isGuest()) {
                return true;
            }
        }
        return false;
    }

    public List<Space> getSpacesWhereRoleIsAdmin() {
        List<Space> list = new ArrayList<Space>();
        for (UserSpaceRole usr : userSpaceRoles) {
            if (usr.isSpaceAdmin()) {
                list.add(usr.getSpace());
            }
        }
        Collections.sort(list);
        return list;
    }

    private Collection<UserSpaceRole> getUserSpaceRolesBySpaceId(long spaceId) {
        List<UserSpaceRole> list = new ArrayList<UserSpaceRole>();
        for (UserSpaceRole usr : userSpaceRoles) {
            if (usr.getSpace() != null && usr.getSpace().getId() == spaceId) {
                list.add(usr);
            }
        }
        return list;
    }

    //============ ACEGI UserDetails implementation ===============

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return !isLocked();
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userSpaceRoles;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    public String getUsername() {
        return getLoginName();
    }


    public int compareTo(User u) {
        if (u == null) {
            return 1;
        }
        if (u.name == null) {
            if (name == null) {
                return 0;
            }
            return 1;
        }
        if (name == null) {
            return -1;
        }
        return name.compareTo(u.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        final User u = (User) o;
        return u.getLoginName().equals(loginName);
    }

    @Override
    public int hashCode() {
        if (loginName == null) {
            return 0;
        }
        return loginName.hashCode();
    }

}
