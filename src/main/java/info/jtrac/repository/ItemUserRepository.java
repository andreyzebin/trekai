package info.jtrac.repository;

import info.jtrac.domain.ItemUser;
import info.jtrac.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemUserRepository extends JpaRepository<ItemUser, Long> {
    List<ItemUser> findByUser(User user);
}
