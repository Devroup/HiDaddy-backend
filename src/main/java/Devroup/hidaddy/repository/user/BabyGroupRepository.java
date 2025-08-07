package Devroup.hidaddy.repository.user;

import Devroup.hidaddy.entity.BabyGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BabyGroupRepository extends JpaRepository<BabyGroup, Long> {

    @EntityGraph(attributePaths = "babies")
    Optional<BabyGroup> findWithBabiesById(Long id);
}