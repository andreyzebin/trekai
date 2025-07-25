package info.jtrac.service;

import info.jtrac.domain.Item;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface JtracService {

    User findUserByLoginName(String loginName);

    List<User> findAllUsers();

    void saveUser(User user);

    Item findItemById(Long id);

    List<Item> findAllItems();

    void saveItem(Item item);

    Space findSpaceById(Long id);

    void saveSpace(Space space);

    void storeSpace(Space space);

    Space loadSpace(long id);

    void removeSpace(long id);

    UserSpaceRole loadUserSpaceRole(long id);

    void storeUserSpaceRole(UserSpaceRole userSpaceRole);

    UserSpaceRole findUserSpaceRoleByUserIdAndSpaceId(long userId, long spaceId);

}
