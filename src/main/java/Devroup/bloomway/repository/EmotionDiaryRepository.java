package Devroup.bloomway.repository;

import Devroup.bloomway.entity.EmotionDiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmotionDiaryRepository extends JpaRepository<EmotionDiary, Integer> {

}