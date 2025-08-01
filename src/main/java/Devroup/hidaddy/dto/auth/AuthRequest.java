package Devroup.hidaddy.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {
    private String provider;  // google, kakao, naver
    private String code;      // Authorization Code
}