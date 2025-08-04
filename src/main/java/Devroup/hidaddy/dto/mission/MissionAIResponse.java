package Devroup.hidaddy.dto.mission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MissionAIResponse {
    private String result; // "PASS" 또는 "FAIL"
    private String reason; // 판독 사유
    private String keyword1;
    private String keyword2;
    private String keyword3;
    private String imageUrl;
}
