package Devroup.bloomway.util;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OAuthUserInfoExtractor {
    public Map<String, String> extract(OAuth2User oauthUser, String login_type) {
        String social_id = null;
        String email = null;

        switch (login_type) {
            case "google" -> {
                social_id = oauthUser.getAttribute("sub");
                email = oauthUser.getAttribute("email");
            }
            case "kakao" -> {
                Object idObj = oauthUser.getAttribute("id");
                social_id = idObj != null ? idObj.toString() : null;

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

                    social_id = idObj != null ? idObj.toString() : null;
                    email = emailObj != null ? emailObj.toString() : null;

                } else {
                    throw new IllegalStateException("네이버 로그인 응답의 형식이 올바르지 않습니다.");
                }
            }

        }

        return Map.of(
                "login_type", login_type,
                "social_id", social_id,
                "email", email
        );
    }
}
