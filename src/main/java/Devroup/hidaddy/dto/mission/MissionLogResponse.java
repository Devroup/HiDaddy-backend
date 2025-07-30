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
    private String imageUrl;
    private String content;
    private String status;
    private Boolean keyword1Success;
    private Boolean keyword2Success;
    private Boolean keyword3Success;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static MissionLogResponse from(MissionLog missionLog) {
        return MissionLogResponse.builder()
                .id(missionLog.getId())
                .missionId(missionLog.getMission().getId())
                .missionTitle(missionLog.getMission().getTitle())
                .imageUrl(missionLog.getImageUrl())
                .content(missionLog.getContent())
                .status(missionLog.getStatus())
                .keyword1Success(missionLog.getKeyword1Success())
                .keyword2Success(missionLog.getKeyword2Success())
                .keyword3Success(missionLog.getKeyword3Success())
                .createdAt(missionLog.getCreatedAt())
                .completedAt(missionLog.getCompletedAt())
                .build();
    }
}