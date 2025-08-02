package Devroup.hidaddy.repository.weeklycontent;

import Devroup.hidaddy.entity.WeeklyContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeeklyContentRepository extends JpaRepository<WeeklyContent, Long> {

    Optional<WeeklyContent> findByWeek(int week);
<<<<<<< HEAD
}
=======
}
>>>>>>> 5e05b4b (feat: mission keyword ai)
