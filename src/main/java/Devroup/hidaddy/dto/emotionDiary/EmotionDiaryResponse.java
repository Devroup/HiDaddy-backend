package Devroup.hidaddy.dto.emotionDiary;

import Devroup.hidaddy.entity.EmotionDiary;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate; 
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor                             
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmotionDiaryResponse {
    private Long id;
    private String content;
    private String imageUrl;
    private LocalDate date;
    private LocalDateTime updatedAt;

    public static EmotionDiaryResponse from(EmotionDiary emotionDiary) {
        return EmotionDiaryResponse.builder()
                .id(emotionDiary.getId())
                .content(emotionDiary.getContent())
                .imageUrl(emotionDiary.getImageUrl())
                .date(emotionDiary.getDate())
                .updatedAt(emotionDiary.getUpdatedAt())
                .build();
    }
}
