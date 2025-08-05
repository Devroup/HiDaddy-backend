package Devroup.hidaddy.dto.mission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionKeywordResponse {
    private Long missionId;
    private String title;          // 미션 제목
    private String description;    // 미션 설명
    private List<String> keywords; // 키워드 3개
}
