package Devroup.bloomway.controller;

import Devroup.bloomway.util.OAuthUserInfoExtractor;
import Devroup.bloomway.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;
    private final OAuthUserInfoExtractor oauthUserInfoExtractor;

    @GetMapping("/")
    public String index() {
        return "<a href='/oauth2/authorization/google'>구글 로그인</a>" +
                "<a href='/oauth2/authorization/kakao'>카카오 로그인</a>" +
                "<a href='/oauth2/authorization/naver'>네이버 로그인</a>";
    }

    @GetMapping("/login-success")
    public ResponseEntity<Map<String, String>> success(
            @AuthenticationPrincipal OAuth2User oauthUser,
            OAuth2AuthenticationToken authToken
    ) {
        String login_type = authToken.getAuthorizedClientRegistrationId(); // "google", "kakao", "naver"

        Map<String, String> userInfo = oauthUserInfoExtractor.extract(oauthUser, login_type);

        String token = userService.saveOrLoginUser(
                null,
                userInfo.get("email"),
                null,
                null,
                userInfo.get("login_type"),
                userInfo.get("social_id")
        );

        System.out.println("✅ 로그인 성공");
        System.out.println("🔐 발급된 JWT: " + token);
        System.out.println("🙋 유저 정보: " + userInfo);

        return ResponseEntity.ok(Map.of("token", token));
    }


}
