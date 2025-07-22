package Devroup.bloomway.repository.mainpage;

import Devroup.bloomway.entity.Baby;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BabyRepository extends JpaRepository<Baby,Long> {
    Optional<Baby> findByIdAndUserId(Long babyId, Long userId);
}
