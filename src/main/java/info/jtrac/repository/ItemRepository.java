package info.jtrac.repository;

import info.jtrac.domain.Item;
import info.jtrac.domain.Space;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findBySpacePrefixCodeAndSequenceNum(String prefixCode, long sequenceNum);

    List<Item> findBySpace(Space space);

}
