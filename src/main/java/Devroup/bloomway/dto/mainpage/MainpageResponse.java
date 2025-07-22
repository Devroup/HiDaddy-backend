package Devroup.bloomway.dto.mainpage;

import Devroup.bloomway.entity.Baby;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
@AllArgsConstructor
public class MainpageResponse {
    private String nickname;
    private String dDay;
    private String profileImageUrl;
    private String message;

    public static MainpageResponse from(Baby baby) {
        return MainpageResponse.builder()
                .nickname(baby.getName())
                .dDay(formatDday(baby.getDueDate().toLocalDate()))
                .profileImageUrl(baby.getProfileImageUrl())
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
