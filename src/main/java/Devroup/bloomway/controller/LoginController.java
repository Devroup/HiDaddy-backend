package Devroup.bloomway.controller;

import Devroup.bloomway.dto.LogoutRequestDto;
// import Devroup.bloomway.entity.User;
import Devroup.bloomway.repository.RefreshTokenRepository;
// import Devroup.bloomway.security.UserDetailsImpl;
import Devroup.bloomway.service.UserService;
import Devroup.bloomway.util.OAuthUserInfoExtractor;
// import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
// import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/login")
public class LoginController {

    private final UserService userService;
    private final OAuthUserInfoExtractor oauthUserInfoExtractor;
    private final RefreshTokenRepository refreshTokenRepository;

    /*
    @GetMapping("")
    public String index(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html><html><head><title>로그인 테스트</title></head><body>");

        if (userDetails != null) {
            User user = userDetails.getUser();  // ✅ 실제 User 엔티티 꺼냄
            html.append("<h2>로그인!!</h2>");
            html.append("<h3>로그인된 사용자: ").append(user.getEmail()).append("</h3>");
            html.append("<form method='post' action='/logout'>")
                    .append("<button type='submit'>로그아웃</button>")
                    .append("</form>");
        } else {
            html.append("<h2>로그인되지 않았습니다</h2>");
            html.append("<h3>소셜 로그인</h3>")
                    .append("<a href='/oauth2/authorization/google'>")
                    .append("<button>Google 로그인</button>")
                    .append("</a><br><br>")
                    .append("<a href='/oauth2/authorization/kakao'>")
                    .append("<button>Kakao 로그인</button>")
                    .append("</a><br><br>")
                    .append("<a href='/oauth2/authorization/naver'>")
                    .append("<button>Naver 로그인</button>")
                    .append("</a><br>");
        }

        html.append("</body></html>");
        return html.toString();
    }



    @Operation(summary = "로그인 성공 후 토큰 발급")
    @GetMapping("/login-success")
    public ResponseEntity<?> success(
            @AuthenticationPrincipal OAuth2User oauthUser,
            OAuth2AuthenticationToken authToken
    ) {
        String loginType = authToken.getAuthorizedClientRegistrationId();
        Map<String, String> userInfo = oauthUserInfoExtractor.extract(oauthUser, loginType);

        Map<String, String> tokens = userService.saveOrLoginUser(
                null,
                userInfo.get("email"),
                null,
                null,
                loginType,
                userInfo.get("socialId")
        );

        return ResponseEntity.ok(Map.of(
                "accessToken", tokens.get("accessToken"),
                "refreshToken", tokens.get("refreshToken"),
                "message", "로그인 성공"
        ));
    }
     */


    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequestDto logoutDto) {
        String refreshToken = logoutDto.getRefreshToken();

        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(refreshTokenRepository::delete);
        }

        return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }
}
