package Devroup.hidaddy.dto.mission;

import Devroup.hidaddy.entity.MissionLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MissionLogResponse {
    private Long id;
    private Long missionId;
    private String missionTitle;
    private LocalDateTime createdAt;

    public static MissionLogResponse from(MissionLog missionLog) {
        return MissionLogResponse.builder()
                .id(missionLog.getId())
                .missionId(missionLog.getMission().getId())
                .missionTitle(missionLog.getMission().getTitle())
                .createdAt(missionLog.getCreatedAt())
                .build();
    }
}