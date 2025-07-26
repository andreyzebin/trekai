package info.jtrac.repository;

import info.jtrac.domain.UserSpaceRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.List;

public interface UserSpaceRoleRepository extends JpaRepository<UserSpaceRole, Long> {
    UserSpaceRole findByUserIdAndSpaceId(long userId, long spaceId);
    List<UserSpaceRole> findBySpaceId(long spaceId);
    List<UserSpaceRole> findByUserId(long userId);
    void deleteBySpaceId(long spaceId);
}
