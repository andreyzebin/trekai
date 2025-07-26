package info.jtrac.repository;

import info.jtrac.domain.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long>, JpaSpecificationExecutor<Space> {
    Optional<Space> findByPrefixCode(String prefixCode);
    List<Space> findByGuestAllowed(boolean guestAllowed);
}

