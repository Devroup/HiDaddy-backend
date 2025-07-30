package Devroup.hidaddy.dto.user;

import Devroup.hidaddy.entity.Baby;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
@AllArgsConstructor
public class SelectedBabyResponse {
    private String nickname;
    private String dDay;
    private String babyImageUrl;
    private String message;

    public static SelectedBabyResponse from(Baby baby) {
        return SelectedBabyResponse.builder()
                .nickname(baby.getName())
                .dDay(formatDday(baby.getDueDate().toLocalDate()))
                .babyImageUrl(baby.getBabyImageUrl())
                .message(baby.getMessage())
                .build();
    }

    public static String formatDday(LocalDate date) {
        int diff = (int) ChronoUnit.DAYS.between(LocalDate.now(), date);
        if (diff == 0)
            return "D-day";

        return (diff > 0) ? "D-" + diff : "D+" + Math.abs(diff);
    }
} 