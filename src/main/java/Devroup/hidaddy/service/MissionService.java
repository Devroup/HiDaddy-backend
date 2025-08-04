package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.mission.*;
import Devroup.hidaddy.entity.Mission;
import Devroup.hidaddy.entity.MissionLog;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.entity.Baby;
import Devroup.hidaddy.entity.WeeklyContent;
import Devroup.hidaddy.repository.mission.MissionLogRepository;
import Devroup.hidaddy.repository.mission.MissionRepository;
import Devroup.hidaddy.repository.emotionDiary.EmotionDiaryRepository;
import Devroup.hidaddy.repository.user.BabyRepository;
import Devroup.hidaddy.repository.weeklycontent.WeeklyContentRepository;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;
import Devroup.hidaddy.util.S3Uploader;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionService {
    private final RestTemplate restTemplate;
    private final MissionRepository missionRepository;
    private final MissionLogRepository missionLogRepository;
    private final EmotionDiaryRepository emotionDiaryRepository;
    private final BabyRepository babyRepository;
    private final WeeklyContentRepository WeeklyContentRepository;
    private final S3Uploader s3Uploader;

    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

    // @Value("${mission.ai.url:http://localhost:6000}")
    @Value("${mission.ai.url:https://devroup.com/mission}")
    private String missionAiUrl;

    public List<MissionLogResponse> getMissionHistory(User user) {
        List<MissionLog> missionLogs = missionLogRepository.findByUserOrderByCreatedAtDesc(user);
        return missionLogs.stream()
                .map(MissionLogResponse::from)
                .collect(Collectors.toList());
    }

    public MissionResponse getMissionDetail(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션을 찾을 수 없습니다."));
        return MissionResponse.from(mission);
    }

    @Transactional
    public MissionKeywordResponse generateMissionForToday(User currentUser) {
        Long selectedBabyId = currentUser.getSelectedBabyId();
        if(selectedBabyId == null)
            throw new IllegalArgumentException("선택된 아이가 없습니다.");

        Baby baby = babyRepository.findByIdAndUserId(
                        currentUser.getSelectedBabyId(),
                        currentUser.getId()
                )
                .orElseThrow(() -> new IllegalArgumentException("해당 아이를 찾을 수 없습니다."));

        int currentWeek = calculateCurrentWeek(baby.getDueDate().toLocalDate());

        List<String> diaries = emotionDiaryRepository.findRecentDiaries(
            currentUser.getId(),
            PageRequest.of(0, 3) // 최근 3개만 가져오기
        );
        
        String guideText = WeeklyContentRepository.findByWeek(currentWeek)
                .map(content -> String.format(
                        "### 아기는 이렇게 자라고 있어요\n\n%s\n\n---\n\n### 아내의 몸도 변화하고 있어요\n\n%s\n\n---\n\n### 아기의 건강, 함께 지켜봐요\n\n%s",
                        content.getBabyContent(),
                        content.getMomContent(),
                        content.getHealthContent()
                ))
                .orElse("해당 주차에 대한 정보가 없습니다.");

        MissionKeywordRequest aiRequest = new MissionKeywordRequest(diaries, guideText);
        
        System.out.println(guideText);
        System.out.println(currentWeek);
        System.out.println(missionAiUrl);

        
        MissionKeywordResponse aiResponse = restTemplate.postForObject(
            // 로컬에서는 공인IP로, 배포환경에서는 localhost로 들어감
            missionAiUrl + "/generate-mission",
            aiRequest,
            MissionKeywordResponse.class
        );
        System.out.println(aiResponse);


        if (aiResponse == null || aiResponse.getKeywords() == null || aiResponse.getKeywords().isEmpty()) {
            throw new RuntimeException("AI 응답에 키워드가 없습니다.");
        }
        
        Mission mission = Mission.builder()
                            .title(aiResponse.getTitle())
                            .description(aiResponse.getDescription())
                            .keyword1(aiResponse.getKeywords().get(0))
                            .keyword2(aiResponse.getKeywords().get(1))
                            .keyword3(aiResponse.getKeywords().get(2))
                            .build();
        
        missionRepository.save(mission);

        aiResponse.setMissionId(mission.getId());
        
        return aiResponse;
    }

    // 현재 주차 계산 (예정일 기준)
    private int calculateCurrentWeek(LocalDate dueDate) {
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        int weeksUntilDue = (int) Math.floor(daysUntilDue / 7.0);  // 남은 주수
        return 40 - weeksUntilDue; // 임신 40주 기준 현재 주차
    }

    @Transactional 
    // 트랜잭션 처리를 보장 -> 모든 작업이 성공적으로 완료되거나 하나라도 실패하면 모든 작업이 롤백됨 -> 데이터 일관성 유지 
    public MissionAIResponse analyzeMissionPhoto(Long missionId, MultipartFile image, User user) {
        Mission mission = missionRepository.findById(missionId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 미션입니다."));

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = s3Uploader.upload(image, "mission");
            imageUrl = cloudFrontDomain + "/" + imageUrl;
        }

        // AI 요청 보내기
        MissionAIRequest aiRequest = MissionAIRequest.builder()
                .title(mission.getTitle())
                .description(mission.getDescription())
                .keyword1(mission.getKeyword1())
                .keyword2(mission.getKeyword2())
                .keyword3(mission.getKeyword3())
                .imageUrl(imageUrl)
                .build();

        MissionAIResponse aiResponse = restTemplate.postForObject(
            missionAiUrl + "/analyze-photo",
            aiRequest,
            MissionAIResponse.class
        );

        if (aiResponse == null || aiResponse.getResult() == null) {
            throw new RuntimeException("AI 판독 결과가 없습니다.");
        }

        // MissionLog 엔티티에 저장
        // -> TODO

        MissionAIResponse analysisResponse = MissionAIResponse.builder()
                            .result(aiResponse.getResult())
                            .reason(aiResponse.getReason())
                            .keyword1(aiResponse.getKeyword1())
                            .keyword2(aiResponse.getKeyword2())
                            .keyword3(aiResponse.getKeyword3())
                            .imageUrl(imageUrl)
                            .build();

        return analysisResponse; // 응답 DTO 생성
    }
} 