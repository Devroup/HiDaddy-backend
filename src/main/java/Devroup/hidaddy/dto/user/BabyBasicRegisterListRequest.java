package Devroup.hidaddy.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class BabyBasicRegisterListRequest {
    private List<BabyRegisterRequest> babies;
}
