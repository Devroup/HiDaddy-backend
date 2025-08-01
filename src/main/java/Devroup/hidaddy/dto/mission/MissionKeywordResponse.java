package Devroup.hidaddy.dto.mission;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissionKeywordResponse {
    private List<String> keywords;
}