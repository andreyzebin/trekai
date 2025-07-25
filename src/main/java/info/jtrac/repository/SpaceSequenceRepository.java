package info.jtrac.repository;

import info.jtrac.domain.SpaceSequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceSequenceRepository extends JpaRepository<SpaceSequence, Long> {
}
