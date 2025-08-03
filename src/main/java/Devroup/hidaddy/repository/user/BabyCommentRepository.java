package Devroup.hidaddy.repository.user;

import Devroup.hidaddy.entity.BabyComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BabyCommentRepository extends JpaRepository<BabyComment, Long> {
    Optional<BabyComment> findByWeekStartLessThanEqualAndWeekEndGreaterThanEqual(int start, int end);
}
