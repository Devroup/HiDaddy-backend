package Devroup.hidaddy.dto.mission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MissionKeywordRequest {
    private List<String> recent_diaries;
    private String guide;
}
