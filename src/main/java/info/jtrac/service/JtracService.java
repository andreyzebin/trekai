package info.jtrac.service;

import info.jtrac.domain.Item;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import info.jtrac.domain.Attachment;
import info.jtrac.domain.CountsHolder;
import info.jtrac.domain.Field;
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

    User loadUser(long id);

    Item storeItem(Item item, Attachment attachment);

    void updateItem(Item item, User user);

    List<Item> findItems(Long spaceId, String summary, Long assignedToId, Integer status);

    List<Item> findItemsByAssignedTo(Long userId);

    List<Item> findItemsByLoggedBy(Long userId);

    CountsHolder loadCountsForUser(User user);

    List<Space> findSpaces(Long userId);

    Space findSpaceByPrefixCode(String prefixCode);

    void removeSpaceByPrefixCode(String prefixCode);

    void addCustomFieldToSpace(String prefixCode, Field field);

    UserSpaceRole loadUserSpaceRole(long id);

    void storeUserSpaceRole(UserSpaceRole userSpaceRole);

    UserSpaceRole findUserSpaceRoleByUserIdAndSpaceId(long userId, long spaceId);

    List<UserSpaceRole> findUserRolesForSpace(long spaceId);

}
