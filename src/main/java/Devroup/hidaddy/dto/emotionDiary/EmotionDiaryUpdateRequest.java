package Devroup.hidaddy.dto.emotionDiary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor     
@Builder    
public class EmotionDiaryUpdateRequest {
    private String content;

    private String imageUrl;
}
