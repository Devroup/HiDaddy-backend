package Devroup.hidaddy.dto.mission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MissionHistoryResponse {
    private List<MissionLogResponse> missionLogList;
    private Boolean isTodayCompleted;
}