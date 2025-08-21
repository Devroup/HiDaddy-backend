package Devroup.hidaddy.dto.user;

import lombok.Getter;
import java.util.List;

@Getter
public class BabyRegisterListRequest {
    private String userName;
    private List<BabyRegisterRequest> babies;
}
