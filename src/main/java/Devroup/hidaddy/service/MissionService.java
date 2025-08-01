package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.mission.*;
import Devroup.hidaddy.entity.Mission;
import Devroup.hidaddy.entity.MissionLog;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.entity.Baby;
import Devroup.hidaddy.repository.mission.MissionLogRepository;
import Devroup.hidaddy.repository.mission.MissionRepository;
import Devroup.hidaddy.repository.emotionDiary.EmotionDiaryRepository;
import Devroup.hidaddy.repository.user.BabyRepository;
import org.springframework.web.client.RestTemplate;
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

    public List<String> generateMissionForToday(User currentUser) {
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

        // 일단은 임시로 mock data
        String guideText = """
        ### 아기는 이렇게 자라고 있어요

        이제 아기는 ‘태아(胎芽)’ 단계를 지나 본격적인 ‘태아(胎兒)’로 불릴 시기에 접어들었어요. 물고기처럼 보였던 꼬리 부분은 거의 사라지고, 몸도 점점 길어지며 곧게 펴지고 있어요.

        손가락과 발가락이 5개씩 분명히 보이고, 눈꺼풀과 입술, 턱, 뺨 등 얼굴 뼈대도 조금씩 자리잡고 있어요. 귀는 안쪽과 바깥쪽으로 분화되고, 콧구멍 모양도 초음파에서 확인될 정도로 정교해지고 있어요.

        ---

        ### 엄마의 몸도 이렇게 변하고 있어요

        자궁은 주먹만 한 크기로 커졌고, 치골 바로 위에서 단단한 느낌으로 만져질 수 있어요. 겉으로 배가 부르진 않지만, 안쪽에서는 공간이 점점 확보되고 있는 거예요.

        커진 자궁이 방광을 압박하면서 소변을 자주 보게 되고, 질과 외음부에 혈류가 많아지면서 색이 진해지거나 분비물이 늘 수 있어요. 일반적인 분비물은 몸이 감염을 막기 위한 자연스러운 반응이지만, 색이 노랗거나 가렵다면 병원 진료가 필요할 수 있어요.

        ---

        ### 아기의 건강, 아빠도 함께 지켜봐요

        **태아 DNA 검사**는 비용이 높은 편이지만, 가족력이나 유전 질환에 대한 우려가 있다면 고려할 수 있어요. 초음파나 일반 기형아 검사로는 알 수 없는 유전성 질환을 보다 정밀하게 확인할 수 있습니다. 6~11주 사이에 검사하는 것이 가장 효과적이며, 필요 여부는 산부인과에서 상담 가능합니다.

        이 시기는 **유산 가능성**이 여전히 높은 시기입니다. 특히 계류유산은 증상이 거의 없어서 입덧이 갑자기 사라지거나 유방 통증이 없어지는 것처럼 작은 변화가 단서가 되기도 해요. 변화가 느껴진다면 지나치지 말고 병원을 방문하는 게 좋습니다.

        **장거리 이동, 과격한 움직임, 무거운 짐, 오래 쪼그려 앉기 등은 최대한 피해야** 하고, 변비도 유산 위험을 높일 수 있어 평소 식이섬유 섭취에도 신경 써야 해요.
        """;

        MissionKeywordRequest aiRequest = new MissionKeywordRequest(diaries, guideText);

        MissionKeywordResponse aiResponse = restTemplate.postForObject(
            "http://localhost:6000/generate-mission",
            aiRequest,
            MissionKeywordResponse.class
        );

        if (aiResponse == null || aiResponse.getKeywords() == null || aiResponse.getKeywords().isEmpty()) {
            throw new RuntimeException("AI 응답에 키워드가 없습니다.");
        }

        // 5. 미션 DB 저장
        // Mission mission = new Mission(user, aiResponse.getKeywords());
        // missionRepository.save(mission);

        return aiResponse.getKeywords();
    }

    // 현재 주차 계산 (예정일 기준)
    private int calculateCurrentWeek(LocalDate dueDate) {
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        int weeksUntilDue = (int) Math.ceil(daysUntilDue / 7.0);  // 남은 주수
        return 40 - weeksUntilDue; // 임신 40주 기준 현재 주차
    }
} 