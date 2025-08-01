package Devroup.hidaddy.dto.user;

import Devroup.hidaddy.entity.Baby;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
@AllArgsConstructor
public class SelectedBabyResponse {
    private Long id;
    private String name;
    private String dDay;
    private String babyImageUrl;
    private String comment;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") // 날짜 포맷 지정
    private LocalDate dueDate;

    public static SelectedBabyResponse from(Baby baby, String comment) {
        return SelectedBabyResponse.builder()
                .id(baby.getId())
                .name(baby.getName())
                .dDay(formatDday(baby.getDueDate().toLocalDate()))
                .babyImageUrl(baby.getBabyImageUrl())
                .comment(comment)
                .dueDate(baby.getDueDate().toLocalDate())
                .build();
    }

    public static String formatDday(LocalDate date) {
        int diff = (int) ChronoUnit.DAYS.between(LocalDate.now(), date);
        if (diff == 0)
            return "D-day";

        return (diff > 0) ? "D-" + diff : "D+" + Math.abs(diff);
    }
} 