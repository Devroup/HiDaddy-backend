package Devroup.hidaddy.dto.mission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MissionAIRequest {
    private String title;
    private String description;
    private String keyword1;
    private String keyword2;
    private String keyword3;
    private String imageUrl;
}
