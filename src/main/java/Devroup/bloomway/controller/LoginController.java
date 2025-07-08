package Devroup.bloomway.controller;

import Devroup.bloomway.entity.User;
import Devroup.bloomway.repository.RefreshTokenRepository;
import Devroup.bloomway.service.UserService;
import Devroup.bloomway.util.OAuthUserInfoExtractor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;
    private final OAuthUserInfoExtractor oauthUserInfoExtractor;
    private final RefreshTokenRepository refreshTokenRepository;

    @GetMapping("/")
    public String index(@AuthenticationPrincipal User user,
                        HttpServletRequest request) {
        StringBuilder html = new StringBuilder();

        if (user != null) {
            html.append("<h2>로그인됨: ").append(user.getEmail()).append("</h2>");
            html.append("<form method='post' action='/logout'>")
                    .append("<button type='submit'>로그아웃</button>")
                    .append("</form>");
        } else {
            html.append("<a href='/oauth2/authorization/google'>구글 로그인</a><br>")
                    .append("<a href='/oauth2/authorization/kakao'>카카오 로그인</a><br>")
                    .append("<a href='/oauth2/authorization/naver'>네이버 로그인</a><br>");
        }

        html.append("<h3>현재 쿠키</h3><ul>");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                html.append("<li>").append(cookie.getName())
                        .append(" = ").append(cookie.getValue()).append("</li>");
            }
        } else {
            html.append("<li>쿠키 없음</li>");
        }
        html.append("</ul>");

        return html.toString();
    }

    @GetMapping("/login-success")
    public ResponseEntity<?> success(
            @AuthenticationPrincipal OAuth2User oauthUser,
            OAuth2AuthenticationToken authToken,
            HttpServletResponse response
    ) {
        String loginType = authToken.getAuthorizedClientRegistrationId();
        Map<String, String> userInfo = oauthUserInfoExtractor.extract(oauthUser, loginType);

        Map<String, String> tokens = userService.saveOrLoginUser(
                null,
                userInfo.get("email"),
                null,
                null,
                userInfo.get("loginType"),
                userInfo.get("socialId")
        );

        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(14))
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.status(302)
                .header("Location", "/")
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
                refreshTokenRepository.delete(token);
            });
        }

        ResponseCookie expiredAccess = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie expiredRefresh = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", expiredAccess.toString());
        response.addHeader("Set-Cookie", expiredRefresh.toString());

        return ResponseEntity.status(302)
                .header("Location", "/")
                .build();
    }
}
