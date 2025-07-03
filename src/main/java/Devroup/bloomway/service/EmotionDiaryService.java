package Devroup.bloomway.service;

import Devroup.bloomway.dto.request.EmotionDiaryCreateRequest;
import Devroup.bloomway.entity.EmotionDiary;
import Devroup.bloomway.repository.EmotionDiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmotionDiaryService {

    private final EmotionDiaryRepository diaryRepository;

    public void create(EmotionDiaryCreateRequest dto) {
        EmotionDiary diary = EmotionDiary.builder()
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .date(dto.getDate().toLocalDate())
                .build();

        diaryRepository.save(diary);
    }
}
