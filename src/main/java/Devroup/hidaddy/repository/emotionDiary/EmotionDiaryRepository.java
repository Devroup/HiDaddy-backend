package Devroup.hidaddy.repository.emotionDiary;

import Devroup.hidaddy.entity.EmotionDiary;
import Devroup.hidaddy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmotionDiaryRepository extends JpaRepository<EmotionDiary, Integer> {

    List<EmotionDiary> findAllByUserIdAndDateBetweenOrderByDateAsc(Long id, LocalDate start, LocalDate end);

    Optional<EmotionDiary> findByUserIdAndDate(User currentUser, LocalDate date);

    @Query("SELECT d.content FROM EmotionDiary d WHERE d.user.id = :userId ORDER BY d.date DESC")
    List<String> findRecentDiaries(@Param("userId") Long userId, Pageable pageable);

}