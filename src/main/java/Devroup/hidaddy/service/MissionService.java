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

    @Value("${mission.ai.url:http://3.36.201.162/:6000}")
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
                            .map(WeeklyContent::getContent)
                            .orElse("해당 주차에 대한 정보가 없습니다.");

        MissionKeywordRequest aiRequest = new MissionKeywordRequest(diaries, guideText);

        MissionKeywordResponse aiResponse = restTemplate.postForObject(
            "http://3.36.201.162:6000/generate-mission",
            aiRequest,
            MissionKeywordResponse.class
        );

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

        return aiResponse;
    }

    // 현재 주차 계산 (예정일 기준)
    private int calculateCurrentWeek(LocalDate dueDate) {
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        int weeksUntilDue = (int) Math.floor(daysUntilDue / 7.0);  // 남은 주수
        return 40 - weeksUntilDue; // 임신 40주 기준 현재 주차
    }
} 