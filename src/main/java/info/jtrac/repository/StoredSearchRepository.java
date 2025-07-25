package info.jtrac.repository;

import info.jtrac.domain.StoredSearch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredSearchRepository extends JpaRepository<StoredSearch, Long> {
}
