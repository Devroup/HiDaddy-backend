package Devroup.bloomway.repository;

import Devroup.bloomway.entity.EmotionDiary;
import Devroup.bloomway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmotionDiaryRepository extends JpaRepository<EmotionDiary, Integer> {

    List<EmotionDiary> findAllByUserIdAndDateBetween(Long id, LocalDate start, LocalDate end);

    Optional<EmotionDiary> findByUserIdAndDate(User currentUser, LocalDate date);
}