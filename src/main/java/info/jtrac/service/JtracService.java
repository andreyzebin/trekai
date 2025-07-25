package info.jtrac.service;

import info.jtrac.domain.Item;
import info.jtrac.domain.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface JtracService {

    User findUserByLoginName(String loginName);

    List<User> findAllUsers();

    void saveUser(User user);

    Item findItemById(Long id);

    List<Item> findAllItems();

    void saveItem(Item item);

}
