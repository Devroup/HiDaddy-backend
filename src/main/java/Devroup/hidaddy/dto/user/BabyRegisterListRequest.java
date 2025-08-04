package Devroup.hidaddy.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class BabyRegisterListRequest {
    private String userName;
    private List<BabyRegisterRequest> babies;
}
