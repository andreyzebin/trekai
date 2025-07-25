package info.jtrac.service;

import info.jtrac.domain.Item;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import info.jtrac.repository.ItemRepository;
import info.jtrac.repository.SpaceRepository;
import info.jtrac.repository.UserRepository;
import info.jtrac.repository.UserSpaceRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JtracServiceImpl implements JtracService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final SpaceRepository spaceRepository;
    private final UserSpaceRoleRepository userSpaceRoleRepository;

    @Autowired
    public JtracServiceImpl(UserRepository userRepository, ItemRepository itemRepository, SpaceRepository spaceRepository, UserSpaceRoleRepository userSpaceRoleRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.spaceRepository = spaceRepository;
        this.userSpaceRoleRepository = userSpaceRoleRepository;
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

    @Override
    public Space findSpaceById(Long id) {
        return spaceRepository.findById(id).orElse(null);
    }

    @Override
    public void saveSpace(Space space) {
        spaceRepository.save(space);
    }

    @Override
    public void storeSpace(Space space) {
        if (space.getMetadata() == null) {
            space.setMetadata(new info.jtrac.domain.Metadata());
        }
        spaceRepository.save(space);
    }

    @Override
    public Space loadSpace(long id) {
        return spaceRepository.findById(id).orElse(null);
    }

    @Override
    public void removeSpace(long id) {
        spaceRepository.deleteById(id);
    }

    @Override
    public UserSpaceRole loadUserSpaceRole(long id) {
        return userSpaceRoleRepository.findById(id).orElse(null);
    }

    @Override
    public void storeUserSpaceRole(UserSpaceRole userSpaceRole) {
        userSpaceRoleRepository.save(userSpaceRole);
    }

    @Override
    public UserSpaceRole findUserSpaceRoleByUserIdAndSpaceId(long userId, long spaceId) {
        return userSpaceRoleRepository.findByUserIdAndSpaceId(userId, spaceId);
    }

    @Override
    public List<UserSpaceRole> findUserRolesForSpace(long spaceId) {
        return userSpaceRoleRepository.findBySpaceId(spaceId);
    }
}
