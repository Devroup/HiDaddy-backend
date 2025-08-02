package Devroup.hidaddy.dto.weeklycontent;

import Devroup.hidaddy.entity.WeeklyContent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class WeeklyContentSimpleResponse {
    private int week;
    private String babyContent;
    private String momContent;
    private String healthContent;

    public static WeeklyContentSimpleResponse from(WeeklyContent weeklyContent) {
        return WeeklyContentSimpleResponse.builder()
                .week(weeklyContent.getWeek())
                .babyContent(weeklyContent.getBabyContent())
                .momContent(weeklyContent.getMomContent())
                .healthContent(weeklyContent.getHealthContent())
                .build();
    }
}
