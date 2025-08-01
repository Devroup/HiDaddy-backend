package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.emotionDiary.*;
import Devroup.hidaddy.entity.EmotionDiary;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.repository.emotionDiary.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmotionDiaryService {

    private final EmotionDiaryRepository diaryRepository;

    public void create(EmotionDiaryCreateRequest dto, User currentUser) {
        LocalDate dataToSave = (dto.getDate() == null) ? LocalDate.now() : dto.getDate();
        EmotionDiary diary = EmotionDiary.builder()
                .user(currentUser)
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .date(dataToSave)
                .build();

        diaryRepository.save(diary);
    }

    // 공통 조회 로직 실패 시 오류 발생
    private EmotionDiary findDiaryOrThrow(Long currentUser, LocalDate date) {
        return diaryRepository.findByUserIdAndDate(currentUser, date)
                .orElseThrow(()-> new IllegalArgumentException("해당 날짜에 작성된 감정일기가 없습니다."));
    }

    // 감정일기 조회(read)
    // 캘린더 용 목록 조회
    @Transactional(readOnly = true)
    public List<EmotionDiaryMonthResponse> readEmotionDiary(User currentUser, LocalDate start, LocalDate end)
    {
        List<EmotionDiary> diaries = diaryRepository.findAllByUserIdAndDateBetweenOrderByDateAsc(
                currentUser.getId(),
                start,
                end
        );

        return diaries.stream()
                .map(EmotionDiaryMonthResponse::from)
                .toList();
    }

    // 개별 감정일기에 접근
    @Transactional(readOnly = true)
    public EmotionDiaryResponse readEmotionDiaryByDate(User currentUser, LocalDate date) {
        return diaryRepository.findByUserIdAndDate(currentUser.getId(), date)
                .map(EmotionDiaryResponse::from)
                .orElseGet(EmotionDiaryResponse::new);
    }

    // 감정일기 수정 (update)

    public EmotionDiaryResponse updateEmotionDiary(
            User currentUser,
            LocalDate date,
            EmotionDiaryUpdateRequest dto
    ) {
        EmotionDiary diary = findDiaryOrThrow(currentUser.getId(), date);
        diary.update(dto.getContent(), dto.getImageUrl());

        return EmotionDiaryResponse.from(diary);
    }

    // 감정일기 삭제 (delete)
    public void deleteEmotionDiary(User currentUser, LocalDate date) {
        EmotionDiary diary = findDiaryOrThrow(currentUser.getId(), date);

        diaryRepository.delete(diary);
    }

}
