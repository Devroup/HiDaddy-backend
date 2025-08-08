package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.weeklycontent.WeeklyContentResponse;
import Devroup.hidaddy.dto.weeklycontent.WeeklyContentSimpleResponse;
import Devroup.hidaddy.entity.Baby;
import Devroup.hidaddy.entity.BabyGroup;
import Devroup.hidaddy.entity.WeeklyContent;
import Devroup.hidaddy.repository.user.BabyGroupRepository;
import Devroup.hidaddy.repository.user.BabyRepository;
import Devroup.hidaddy.repository.weeklycontent.WeeklyContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyContentService {
    private final BabyGroupRepository babyGroupRepository;
    private final WeeklyContentRepository weeklyContentRepository;

    // 출산 예정일로부터 현재 날짜 계산하여 해당 주차 컨텐츠 출력
    public WeeklyContentResponse getWeeklyContent(Long groupId) {
        BabyGroup group = babyGroupRepository.findWithBabiesById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("선택된 아기 그룹을 찾을 수 없습니다."));

        List<Baby> babies = group.getBabies();

        int currentweek = caculateCurrentWeek(babies.get(0).getDueDate().toLocalDate());

        WeeklyContent weeklyContent = weeklyContentRepository.findByWeek(currentweek)
                .orElseThrow(() -> new IllegalArgumentException(currentweek + "주차 데이터가 없습니다."));

        return WeeklyContentResponse.from(currentweek, weeklyContent);
    }

    // 화살표로 특정 주차 넘어갈 경우 특정 주차의 컨텐츠 출력
    public WeeklyContentSimpleResponse getWeeklyContentSimple(int week) {
        WeeklyContent weeklyContent = weeklyContentRepository.findByWeek(week)
                .orElseThrow(() -> new IllegalArgumentException(week + "주차 데이터가 없습니다."));

        return WeeklyContentSimpleResponse.from(weeklyContent);
    }

    // 현재 날짜를 기준으로 출산 예정일로부터 주차 계산
    private int caculateCurrentWeek(LocalDate dueDate) {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        long weeksLeft = daysLeft / 7;
        return (int) (40 - weeksLeft);
    }
}
