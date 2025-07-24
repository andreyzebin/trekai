package info.jtrac.service;

import info.jtrac.domain.Item;
import info.jtrac.domain.User;
import info.jtrac.repository.ItemRepository;
import info.jtrac.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JtracServiceImpl implements JtracService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public JtracServiceImpl(UserRepository userRepository, ItemRepository itemRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<User> users = userRepository.findByLoginName(username);
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return users.get(0);
    }

    @Override
    public User findUserByLoginName(String loginName) {
        List<User> users = userRepository.findByLoginName(loginName);
        return users.isEmpty() ? null : users.get(0);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public Item findItemById(Long id) {
        return itemRepository.findById(id).orElse(null);
    }

    @Override
    public List<Item> findAllItems() {
        return itemRepository.findAll();
    }

    @Override
    public void saveItem(Item item) {
        itemRepository.save(item);
    }
}
