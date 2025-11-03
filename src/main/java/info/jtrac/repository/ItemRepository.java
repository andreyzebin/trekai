package info.jtrac.repository;

import info.jtrac.domain.Item;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    List<Item> findBySpace(Space space);

    List<Item> findBySpacePrefixCodeAndSequenceNum(String prefixCode, long sequenceNum);

    List<Item> findByAssignedToId(Long assignedToId);

    List<Item> findByLoggedById(Long loggedById);

    long countBySpaceAndLoggedBy(Space space, User user);

    long countBySpaceAndAssignedTo(Space space, User user);

    void deleteBySpaceId(long spaceId);

    @Query("SELECT i FROM Item i " +
            "LEFT JOIN FETCH i.assignedTo " +
            "LEFT JOIN FETCH i.loggedBy " +
            "LEFT JOIN FETCH i.space s " +
            "LEFT JOIN FETCH s.metadata " +
            "WHERE i.id = :id")
    Optional<Item> findItemByIdWithAssociations(@Param("id") Long id);
}


