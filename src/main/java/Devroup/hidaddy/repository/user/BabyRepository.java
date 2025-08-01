package Devroup.hidaddy.repository.user;

import Devroup.hidaddy.entity.Baby;
import Devroup.hidaddy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BabyRepository extends JpaRepository<Baby, Long> {

    // 특정 유저의 모든 아기 목록 조회
    List<Baby> findByUser(User user);

    void deleteAllByUser(User user);

    Optional<Baby> findByIdAndUserId(Long babyId, Long userId);

    Optional<Baby> findById(Long babyId);
}
