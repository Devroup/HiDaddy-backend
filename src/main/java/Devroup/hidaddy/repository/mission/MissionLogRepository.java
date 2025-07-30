package Devroup.hidaddy.repository.mission;

import Devroup.hidaddy.entity.MissionLog;
import Devroup.hidaddy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionLogRepository extends JpaRepository<MissionLog, Long> {
    List<MissionLog> findByUserOrderByCreatedAtDesc(User user);
} 