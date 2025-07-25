package info.jtrac.repository;

import info.jtrac.domain.ItemItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemItemRepository extends JpaRepository<ItemItem, Long> {
}
