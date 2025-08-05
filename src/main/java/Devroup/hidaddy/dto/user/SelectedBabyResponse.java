package Devroup.hidaddy.dto.user;

import Devroup.hidaddy.entity.Baby;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SelectedBabyResponse {
    private List<BabyResponse> babies;
    private String comment;
    private String dDay;

    public static SelectedBabyResponse from(Baby baby, String comment) {
        return SelectedBabyResponse.builder()
                .dDay(formatDday(baby.getDueDate().toLocalDate()))
                .comment(comment)
                .build();
    }

    public static String formatDday(LocalDate date) {
        int diff = (int) ChronoUnit.DAYS.between(LocalDate.now(), date);
        if (diff == 0)
            return "D-day";
        return (diff > 0) ? "D-" + diff : "D+" + Math.abs(diff);
    }
}
