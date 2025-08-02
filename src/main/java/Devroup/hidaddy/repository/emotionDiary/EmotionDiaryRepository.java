package Devroup.hidaddy.repository.emotionDiary;

import Devroup.hidaddy.entity.EmotionDiary;
import Devroup.hidaddy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmotionDiaryRepository extends JpaRepository<EmotionDiary, Integer> {

    List<EmotionDiary> findAllByUserIdAndDateBetweenOrderByDateAsc(Long id, LocalDate start, LocalDate end);

    Optional<EmotionDiary> findByUserIdAndDate(Long currentUser, LocalDate date);
}