package Devroup.bloomway.repository;

import Devroup.bloomway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.social_id = :socialId AND u.login_type = :loginType")
    Optional<User> findBySocialIdAndLoginType(@Param("socialId") String socialId, @Param("loginType") String loginType);
}
