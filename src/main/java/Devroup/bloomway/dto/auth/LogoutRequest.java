package Devroup.bloomway.dto.auth;

import lombok.Getter;

@Getter
public class LogoutRequest {
    private String refreshToken;
}