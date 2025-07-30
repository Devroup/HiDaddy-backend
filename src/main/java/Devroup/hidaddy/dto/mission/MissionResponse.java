package Devroup.hidaddy.dto.mission;

import Devroup.hidaddy.entity.Mission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MissionResponse {
    private Long id;
    private String title;
    private String description;
    private String keyword1;
    private String keyword2;
    private String keyword3;
    private LocalDateTime createdAt;

    public static MissionResponse from(Mission mission) {
        return MissionResponse.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .keyword1(mission.getKeyword1())
                .keyword2(mission.getKeyword2())
                .keyword3(mission.getKeyword3())
                .createdAt(mission.getCreatedAt())
                .build();
    }
} 