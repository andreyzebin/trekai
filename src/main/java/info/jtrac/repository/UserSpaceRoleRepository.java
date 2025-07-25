package info.jtrac.repository;

import info.jtrac.domain.UserSpaceRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSpaceRoleRepository extends JpaRepository<UserSpaceRole, Long> {

    List<UserSpaceRole> findByUserId(long userId);

    List<UserSpaceRole> findBySpaceId(long spaceId);

    UserSpaceRole findByUserIdAndSpaceId(long userId, long spaceId);
}
