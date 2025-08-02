package Devroup.hidaddy.dto.emotionDiary;

import lombok.Builder;
import lombok.Getter;
import Devroup.hidaddy.entity.EmotionDiary;

import java.time.LocalDate;

@Getter
@Builder
public class EmotionDiaryMonthResponse {
    private LocalDate date;

    public static EmotionDiaryMonthResponse from(EmotionDiary emotionDiary) {
        return EmotionDiaryMonthResponse.builder()
                .date(emotionDiary.getDate())
                .build();
    }
}
