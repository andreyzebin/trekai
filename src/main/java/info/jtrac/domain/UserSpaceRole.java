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

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@NoArgsConstructor
@Data
@Entity
@Table(name = "user_space_roles")
public class UserSpaceRole implements GrantedAuthority, Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @ManyToOne
    @JsonBackReference
    private User user;
    
    @ManyToOne
    @JsonBackReference
    private Space space;
    
    private String roleKey;    

    public UserSpaceRole(User user, Space space, String roleKey) {
        this.user = user;
        this.space = space;
        this.roleKey = roleKey;
    }

    
    public boolean isSuperUser() {
        return space == null && isAdmin();
    }
    
    public boolean isSpaceAdmin() {
        return space != null && isAdmin();
    }    
    
    public boolean isAdmin() {
        return Role.ROLE_ADMIN.equals(roleKey);
    }
    
    public boolean isGuest() {
        return Role.ROLE_GUEST.equals(roleKey);
    }
    
    //======== ACEGI GrantedAuthority implementation =============
    
    public String getAuthority() {        
        if (space != null) {
            return roleKey + ":" + space.getPrefixCode();
        }
        return roleKey;
    }
    
    //=============================================================      


    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserSpaceRole)) {
            return false;
        }
        final UserSpaceRole usr = (UserSpaceRole) o;
        return (
            (space == usr.getSpace() || space.equals(usr.getSpace()))
            && user.equals(usr.getUser())
            && roleKey.equals(usr.getRoleKey())
        );
    }

    
    @Override
    public String toString() {
        return getAuthority();
    }
    
}
