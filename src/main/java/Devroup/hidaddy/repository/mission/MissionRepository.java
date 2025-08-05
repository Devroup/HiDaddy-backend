package Devroup.hidaddy.repository.mission;

import Devroup.hidaddy.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.time.LocalDate;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {
    Optional<Mission> findByUserIdAndDate(Long userId, LocalDate date);
} 