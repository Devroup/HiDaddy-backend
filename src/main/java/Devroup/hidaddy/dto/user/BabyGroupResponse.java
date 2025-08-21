package Devroup.hidaddy.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BabyGroupResponse {
    private Long babyGroupId;
    private List<BabyResponse> babies;
}