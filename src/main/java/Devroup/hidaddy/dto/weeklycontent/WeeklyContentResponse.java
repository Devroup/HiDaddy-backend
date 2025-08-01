package Devroup.hidaddy.dto.weeklycontent;

import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.entity.WeeklyContent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WeeklyContentResponse {
    private Long userId;
    private int currentWeek;
    private String babyContent;
    private String momContent;
    private String healthContent;

    public static WeeklyContentResponse from(int currentWeek, WeeklyContent weeklyContent) {
        return WeeklyContentResponse.builder()
                .currentWeek(currentWeek)
                .babyContent(weeklyContent.getBabyContent())
                .momContent(weeklyContent.getMomContent())
                .healthContent(weeklyContent.getHealthContent())
                .build();
    }
}
