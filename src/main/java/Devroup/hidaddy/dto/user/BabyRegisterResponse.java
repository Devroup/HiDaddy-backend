package Devroup.hidaddy.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BabyRegisterResponse {
    private String userName;
    private List<BabyResponse> babies;
}
