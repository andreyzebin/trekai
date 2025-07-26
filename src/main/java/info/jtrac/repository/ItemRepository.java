package info.jtrac.repository;

import info.jtrac.domain.Item;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    List<Item> findBySpace(Space space);
    List<Item> findBySpacePrefixCodeAndSequenceNum(String prefixCode, long sequenceNum);
    List<Item> findByAssignedToId(Long assignedToId);
    List<Item> findByLoggedById(Long loggedById);
    long countBySpaceAndLoggedBy(Space space, User user);
    long countBySpaceAndAssignedTo(Space space, User user);
    void deleteBySpaceId(long spaceId);
}


