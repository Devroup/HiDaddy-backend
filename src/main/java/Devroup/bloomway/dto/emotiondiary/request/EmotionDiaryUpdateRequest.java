package Devroup.bloomway.dto.emotiondiary.request;

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
