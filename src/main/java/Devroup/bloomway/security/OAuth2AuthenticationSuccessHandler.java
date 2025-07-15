package Devroup.bloomway.security;

import Devroup.bloomway.service.UserService;
import Devroup.bloomway.util.OAuthUserInfoExtractor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final UserService userService;
    private final OAuthUserInfoExtractor oauthUserInfoExtractor;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("✅ OAuth2 로그인 성공 - 인증 객체 수신");

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();

        String loginType = oauthToken.getAuthorizedClientRegistrationId(); // ex. google, naver
        log.info("✅ 로그인 타입: {}", loginType);

        Map<String, String> userInfo = oauthUserInfoExtractor.extract(oauthUser, loginType);
        log.info("✅ 사용자 정보 추출됨: {}", userInfo);

        // User 생성 or 조회 + Token 발급
        Map<String, String> tokens = userService.saveOrLoginUser(
                null,
                userInfo.get("email"),
                null,
                null,
                loginType,
                userInfo.get("socialId")
        );

        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        log.info("✅ 토큰 발급 완료");
        log.debug("🔐 accessToken: {}", accessToken);
        log.debug("🔐 refreshToken: {}", refreshToken);

        // 앱에서 처리할 수 있도록 딥링크로 리디렉션
        String redirectUri = "myapp://login"
                + "?accessToken=" + accessToken
                + "&refreshToken=" + refreshToken;

        log.info("📦 리디렉션 URI: {}", redirectUri);
        response.sendRedirect(redirectUri);
    }
}
