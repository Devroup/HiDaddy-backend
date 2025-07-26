package Devroup.bloomway.repository.user;

import Devroup.bloomway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialIdAndLoginType(String socialId, String loginType);
    Optional<User> findBySocialId(String socialId);
}
