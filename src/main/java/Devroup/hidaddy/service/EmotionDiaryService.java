package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.emotionDiary.*;
import Devroup.hidaddy.entity.EmotionDiary;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.repository.emotionDiary.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import Devroup.hidaddy.util.S3Uploader;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmotionDiaryService {
    private final S3Uploader s3Uploader;
    private final EmotionDiaryRepository diaryRepository;
    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

    public EmotionDiaryResponse create(EmotionDiaryCreateRequest dto, MultipartFile image, User currentUser) {
        LocalDate dataToSave = (dto.getDate() == null) ? LocalDate.now() : dto.getDate();
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = s3Uploader.upload(image, "emotionDiary");
            imageUrl = cloudFrontDomain + "/" + imageUrl;
        }

        EmotionDiary diary = EmotionDiary.builder()
                .user(currentUser)
                .content(dto.getContent())
                .imageUrl(imageUrl)
                .date(dataToSave)
                .build();

        diaryRepository.save(diary);
        return EmotionDiaryResponse.from(diary);
    }

    // 공통 조회 로직 실패 시 오류 발생
    private EmotionDiary findDiaryOrThrow(Long userId, LocalDate date) {
        return diaryRepository.findByUserIdAndDate(userId, date)
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
            EmotionDiaryUpdateRequest dto,
            MultipartFile image
    ) {
        EmotionDiary diary = findDiaryOrThrow(currentUser.getId(), date);
        diary.update(dto.getContent());

        if (image != null && !image.isEmpty()) {
            // 기존 이미지 삭제
            if (diary.getImageUrl() != null && !diary.getImageUrl().isEmpty()) {
                String imageKey = diary.getImageUrl().replace(cloudFrontDomain + "/", "");
                s3Uploader.delete(imageKey);
            }
            // 새 이미지 업로드
            String imageUrl = s3Uploader.upload(image, "emotionDiary");
            imageUrl = cloudFrontDomain + "/" + imageUrl;
            diary.setImageUrl(imageUrl);
        }

        return EmotionDiaryResponse.from(diary);
    }

    // 감정일기 삭제 (delete)
    public void deleteEmotionDiary(User currentUser, LocalDate date) {
        EmotionDiary diary = findDiaryOrThrow(currentUser.getId(), date);

        diaryRepository.delete(diary);
    }

}
