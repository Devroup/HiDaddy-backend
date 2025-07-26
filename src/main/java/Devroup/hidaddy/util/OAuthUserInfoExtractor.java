package Devroup.hidaddy.util;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OAuthUserInfoExtractor {
    // 플랫폼별 유저 정보 추출
    public Map<String, String> extract(OAuth2User oauthUser, String loginType) {
        String socialId = null;
        String email = null;

        switch (loginType) {
            case "google" -> {      
                socialId = oauthUser.getAttribute("sub");
                email = oauthUser.getAttribute("email");
            }
            case "kakao" -> {
                Object idObj = oauthUser.getAttribute("id");
                socialId = idObj != null ? idObj.toString() : null;

                Map<String, Object> kakaoAccount = oauthUser.getAttribute("kakao_account");
                if (kakaoAccount != null) {
                    email = (String) kakaoAccount.get("email");

                } else {
                    email = null;

                }
            }
            case "naver" -> {
                Object responseObj = oauthUser.getAttribute("response");

                if (responseObj instanceof Map<?, ?> map) {
                    Object idObj = map.get("id");
                    Object emailObj = map.get("email");

                    socialId = idObj != null ? idObj.toString() : null;
                    email = emailObj != null ? emailObj.toString() : null;

                } else {
                    throw new IllegalStateException("네이버 로그인 응답의 형식이 올바르지 않습니다.");
                }
            }

        }

        return Map.of(
                "loginType", loginType,
                "socialId", socialId,
                "email", email
        );
    }
}
