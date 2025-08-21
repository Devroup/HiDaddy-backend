package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.mission.*;
import Devroup.hidaddy.entity.Mission;
import Devroup.hidaddy.entity.MissionLog;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.entity.Baby;
import Devroup.hidaddy.entity.BabyGroup;
import Devroup.hidaddy.entity.WeeklyContent;
import Devroup.hidaddy.repository.mission.MissionLogRepository;
import Devroup.hidaddy.repository.mission.MissionRepository;
import Devroup.hidaddy.repository.emotionDiary.EmotionDiaryRepository;
import Devroup.hidaddy.repository.user.BabyRepository;  
import Devroup.hidaddy.repository.user.BabyGroupRepository;
import Devroup.hidaddy.repository.weeklycontent.WeeklyContentRepository;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;
import Devroup.hidaddy.util.S3Uploader;
import java.util.Optional;
import java.util.Arrays;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class MissionService {
    private final RestTemplate restTemplate;
    private final MissionRepository missionRepository;
    private final MissionLogRepository missionLogRepository;
    private final EmotionDiaryRepository emotionDiaryRepository;
    private final BabyRepository babyRepository;
    private final BabyGroupRepository babyGroupRepository;
    private final WeeklyContentRepository WeeklyContentRepository;
    private final S3Uploader s3Uploader;

    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

    // @Value("${mission.ai.url:http://localhost:6000}")
    @Value("${spring.application.domain}")
    private String missionAiUrl;

    @Transactional(readOnly = true)
    public MissionHistoryResponse getMissionHistory(User user) {
        List<MissionLog> missionLogs = missionLogRepository.findByUserOrderByCreatedAtDesc(user);
        
        LocalDate today = LocalDate.now();
        boolean isTodayCompleted = missionRepository.findByUserIdAndDate(user.getId(), today).isPresent();
        
        List<MissionLogResponse> missionLogList = missionLogs.stream()
                .map(MissionLogResponse::from)
                .collect(Collectors.toList());
        
        return MissionHistoryResponse.builder()
                .missionLogList(missionLogList)
                .isTodayCompleted(isTodayCompleted)
                .build();
    }

    @Transactional(readOnly = true)
    public MissionResponse getMissionDetail(Long missionId, User user) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션을 찾을 수 없습니다."));
        
        Optional<MissionLog> missionLogOpt = missionLogRepository.findByMissionIdAndUserId(missionId, user.getId());

        Boolean keyword1Success = false;
        Boolean keyword2Success = false;
        Boolean keyword3Success = false;
        String imageUrl = null;
        String content = null;

        if (missionLogOpt.isPresent()) {
            MissionLog log = missionLogOpt.get();
            keyword1Success = Boolean.TRUE.equals(log.getKeyword1Success());
            keyword2Success = Boolean.TRUE.equals(log.getKeyword2Success());
            keyword3Success = Boolean.TRUE.equals(log.getKeyword3Success());
            imageUrl = log.getImageUrl();
            content = log.getContent();
        }
        
        // MissionResponse 생성
        return MissionResponse.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .keyword1(mission.getKeyword1())
                .keyword2(mission.getKeyword2())
                .keyword3(mission.getKeyword3())
                .keyword1Success(keyword1Success)
                .keyword2Success(keyword2Success)
                .keyword3Success(keyword3Success)
                .imageUrl(imageUrl)
                .content(content)
                .createdAt(mission.getCreatedAt())
                .build();
    }

    public MissionKeywordResponse getOrCreateTodayMission(User currentUser) {
        // 1차: 빠른 조회
        Optional<Mission> existingMission = getTodayMission(currentUser.getId());
        if (existingMission.isPresent()) {
            return buildMissionResponse(existingMission.get());
        }
        
        // 없으면 생성
        return createTodayMission(currentUser);
    }
    
    @Transactional(readOnly = true)
    public Optional<Mission> getTodayMission(Long userId) {
        LocalDate today = LocalDate.now();
        return missionRepository.findByUserIdAndDate(userId, today);
    }
    
    private MissionKeywordResponse buildMissionResponse(Mission mission) {
        return MissionKeywordResponse.builder()
                .missionId(mission.getId())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .keywords(Arrays.asList(
                        mission.getKeyword1(),
                        mission.getKeyword2(),
                        mission.getKeyword3()
                ))
                .build();
    }
    
    @Transactional
    public MissionKeywordResponse createTodayMission(User currentUser) {
        Long selectedGroupId = currentUser.getSelectedBabyId();
        if (selectedGroupId == null) {
            throw new IllegalArgumentException("선택된 아기 그룹이 없습니다.");
        }

        // 그룹 ID로 BabyGroup 조회
        BabyGroup group = babyGroupRepository.findWithBabiesById(selectedGroupId)
                .orElseThrow(() -> new IllegalArgumentException("선택된 아기 그룹을 찾을 수 없습니다."));

        // 권한 검증
        boolean isUserGroup = group.getBabies().stream()
                .anyMatch(b -> b.getUser().getId().equals(currentUser.getId()));
        if (!isUserGroup) {
            throw new IllegalArgumentException("선택된 아기 그룹에 대한 권한이 없습니다.");
        }

        // Double-check: AI 호출 전에 한번 더 확인
        Optional<Mission> doubleCheck = missionRepository.findByUserIdAndDate(currentUser.getId(), LocalDate.now());
        if (doubleCheck.isPresent()) {
            return buildMissionResponse(doubleCheck.get());
        }

        List<Baby> babies = group.getBabies();
        Baby baseBaby = babies.get(0);
        LocalDate today = LocalDate.now();
        
        // 오늘 미션이 없으면 새로 생성
        int currentWeek = calculateCurrentWeek(baseBaby.getDueDate().toLocalDate());

        // 최근 3개의 감정일기 가져오기
        List<String> diaries = emotionDiaryRepository.findRecentDiaries(
            currentUser.getId(),
            PageRequest.of(0, 3)
        );

        // 주차별 가이드 텍스트 생성
        String guideText = WeeklyContentRepository.findByWeek(currentWeek)
                .map(content -> String.format(
                        "### 아기는 이렇게 자라고 있어요\n\n%s\n\n---\n\n### 아내의 몸도 변화하고 있어요\n\n%s\n\n---\n\n### 아기의 건강, 함께 지켜봐요\n\n%s",
                        content.getBabyContent(),
                        content.getMomContent(),
                        content.getHealthContent()
                ))
                .orElse("해당 주차에 대한 정보가 없습니다.");

        // AI 요청 DTO
        MissionKeywordRequest aiRequest = new MissionKeywordRequest(diaries, guideText);

        // AI 서버 요청 -> 오늘 미션 생성 (트랜잭션 밖에서 호출)
        MissionKeywordResponse aiResponse = callAiService(aiRequest);

        if (aiResponse == null || aiResponse.getKeywords() == null || aiResponse.getKeywords().isEmpty()) {
            throw new RuntimeException("AI 응답에 키워드가 없습니다.");
        }
        
        // 최종 저장 전 한번 더 체크 (매우 드문 케이스 대비)
        Optional<Mission> finalCheck = missionRepository.findByUserIdAndDate(currentUser.getId(), today);
        if (finalCheck.isPresent()) {
            return buildMissionResponse(finalCheck.get());
        }
        
        // Mission DB 저장
        Mission mission = Mission.builder()
                            .title(aiResponse.getTitle())
                            .description(aiResponse.getDescription())
                            .keyword1(aiResponse.getKeywords().get(0))
                            .keyword2(aiResponse.getKeywords().get(1))
                            .keyword3(aiResponse.getKeywords().get(2))
                            .user(currentUser)
                            .date(today)
                            .build();
        
        missionRepository.save(mission);

        // 생성된 미션ID 응답에 저장
        aiResponse.setMissionId(mission.getId());
        
        return aiResponse;
    }
    
    private MissionKeywordResponse callAiService(MissionKeywordRequest aiRequest) {
        return restTemplate.postForObject(
            // 로컬에서는 공인IP로, 배포환경에서는 localhost로 들어감
            missionAiUrl + "/mission/generate-mission",
            aiRequest,
            MissionKeywordResponse.class
        );
    }

    // 현재 주차 계산 (예정일 기준)
    private int calculateCurrentWeek(LocalDate dueDate) {
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        int weeksUntilDue = (int) Math.floor(daysUntilDue / 7.0);  // 남은 주수
        return 40 - weeksUntilDue; // 임신 40주 기준 현재 주차
    }

    @Transactional(readOnly = false)
    // 트랜잭션 처리를 보장 -> 모든 작업이 성공적으로 완료되거나 하나라도 실패하면 모든 작업이 롤백됨 -> 데이터 일관성 유지 
    public MissionAIResponse analyzeMissionPhoto(Long missionId, MultipartFile image, String content, User user) {
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
            missionAiUrl + "/mission/analyze-photo",
            aiRequest,
            MissionAIResponse.class
        );

        if (aiResponse == null || aiResponse.getResult() == null) {
            throw new RuntimeException("AI 판독 결과가 없습니다.");
        }

        // 기존 MissionLog 검색
        Optional<MissionLog> existingLogOpt = missionLogRepository.findByMissionIdAndUserId(missionId, user.getId());

        if (existingLogOpt.isPresent()) {
            MissionLog existingLog = existingLogOpt.get();

            // S3에서 기존 이미지 삭제
            if (existingLog.getImageUrl() != null && !existingLog.getImageUrl().isEmpty()) {
                String imageKey = existingLog.getImageUrl().replace(cloudFrontDomain + "/", "");
                s3Uploader.delete(imageKey);
            }
            // 기존 로그 삭제
            missionLogRepository.delete(existingLog);
        }

        // 새로운 MissionLog 저장
        MissionLog missionLog = MissionLog.builder()
                .mission(mission)
                .user(user)
                .imageUrl(imageUrl)
                .content(content)
                .keyword1Success(aiResponse.getKeyword1())
                .keyword2Success(aiResponse.getKeyword2())
                .keyword3Success(aiResponse.getKeyword3())
                .build();

        missionLogRepository.save(missionLog);
        
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