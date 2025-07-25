package info.jtrac.service;

import info.jtrac.domain.Attachment;
import info.jtrac.domain.History;
import info.jtrac.domain.Item;
import info.jtrac.domain.Space;
import info.jtrac.domain.SpaceSequence;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import info.jtrac.repository.ItemRepository;
import info.jtrac.repository.SpaceRepository;
import info.jtrac.repository.SpaceSequenceRepository;
import info.jtrac.repository.UserRepository;
import info.jtrac.repository.UserSpaceRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class JtracServiceImpl implements JtracService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final SpaceRepository spaceRepository;
    private final UserSpaceRoleRepository userSpaceRoleRepository;
    private final SpaceSequenceRepository spaceSequenceRepository;

    @Autowired
    public JtracServiceImpl(UserRepository userRepository, ItemRepository itemRepository, SpaceRepository spaceRepository, UserSpaceRoleRepository userSpaceRoleRepository, SpaceSequenceRepository spaceSequenceRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.spaceRepository = spaceRepository;
        this.userSpaceRoleRepository = userSpaceRoleRepository;
        this.spaceSequenceRepository = spaceSequenceRepository;
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
        if (space.getId() > 0 && spaceSequenceRepository.findById(space.getId()).isEmpty()) {
            SpaceSequence ss = new SpaceSequence();
            ss.setNextSeqNum(1);
            ss.setId(space.getId());
            spaceSequenceRepository.save(ss);
        }
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
    public User loadUser(long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public Item storeItem(Item item, Attachment attachment) {
        History history = new History();
        history.setLoggedBy(item.getLoggedBy());
        history.setAssignedTo(item.getAssignedTo());
        history.setComment(item.getDetail());
        history.setTimeStamp(new Date());
        history.setStatus(item.getStatus());
        
        if (item.getId() == 0) { // new item
            SpaceSequence ss = spaceSequenceRepository.findById(item.getSpace().getId()).orElseThrow();
            long seqNum = ss.getNextSeqNum();
            ss.setNextSeqNum(seqNum + 1);
            spaceSequenceRepository.save(ss);
            item.setSequenceNum(seqNum);
        }
        
        item.add(history);
        
        if (attachment != null) {
            item.add(attachment);
        }
        
        return itemRepository.save(item);
    }

    @Override
    public void updateItem(Item item, User user) {
        History history = new History();
        history.setLoggedBy(user);
        history.setAssignedTo(item.getAssignedTo());
        history.setComment(item.getEditReason());
        history.setTimeStamp(new Date());
        history.setStatus(item.getStatus());
        item.add(history);
        itemRepository.save(item);
    }

    @Override
    public List<Item> findItems(long spaceId, String summary, Long assignedToId, Integer status) {
        return itemRepository.findAll((Specification<Item>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("space").get("id"), spaceId));
            if (summary != null && !summary.isEmpty()) {
                predicates.add(cb.like(root.get("summary"), "%" + summary + "%"));
            }
            if (assignedToId != null) {
                predicates.add(cb.equal(root.get("assignedTo").get("id"), assignedToId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        });
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
