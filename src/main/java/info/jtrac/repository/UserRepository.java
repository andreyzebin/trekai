package info.jtrac.repository;

import info.jtrac.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByLoginName(String loginName);

    List<User> findByEmail(String email);

}
