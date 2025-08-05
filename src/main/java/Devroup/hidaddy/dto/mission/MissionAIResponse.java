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
    private Boolean keyword1;
    private Boolean keyword2;
    private Boolean keyword3;
    private String imageUrl;
}
