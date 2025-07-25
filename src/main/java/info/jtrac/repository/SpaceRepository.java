package info.jtrac.repository;

import info.jtrac.domain.Space;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpaceRepository extends JpaRepository<Space, Long> {

    List<Space> findByPrefixCode(String prefixCode);

    List<Space> findByGuestAllowed(boolean guestAllowed);
}
