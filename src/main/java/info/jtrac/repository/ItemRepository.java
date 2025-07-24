package info.jtrac.repository;

import info.jtrac.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findBySpacePrefixCodeAndSequenceNum(String prefixCode, long sequenceNum);

}
