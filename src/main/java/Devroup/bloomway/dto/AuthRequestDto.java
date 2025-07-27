package Devroup.bloomway.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequestDto {
    private String provider;  // google, kakao, naver
    private String code;      // Authorization Code
}