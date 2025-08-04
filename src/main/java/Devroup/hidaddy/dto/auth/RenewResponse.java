package Devroup.hidaddy.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RenewResponse {
    private String accessToken;
    private String message;
}