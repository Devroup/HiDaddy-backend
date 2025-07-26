package Devroup.bloomway.repository.user;

import Devroup.bloomway.entity.Baby;
import Devroup.bloomway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BabyRepository extends JpaRepository<Baby, Long> {

    // 특정 유저의 모든 아기 목록 조회
    List<Baby> findByUser(User user);

    void deleteAllByUser(User user);
}
