package Devroup.hidaddy.controller;

import Devroup.hidaddy.entity.RefreshToken;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.jwt.JwtUtil;
import Devroup.hidaddy.dto.auth.AuthRequest;
import Devroup.hidaddy.dto.auth.LogoutRequest;
import Devroup.hidaddy.dto.auth.RefreshTokenRequest;
import Devroup.hidaddy.repository.auth.RefreshTokenRepository;
import Devroup.hidaddy.service.AuthService;
import Devroup.hidaddy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final AuthService authService;

    // Access Token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshTokenRequest requestDto) {
        String refreshToken = requestDto.getRefreshToken();
        RefreshToken foundToken = refreshTokenRepository.findByToken(refreshToken).orElse(null);

        if (foundToken == null || foundToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않거나 만료된 리프레시 토큰");
        }

        User user = foundToken.getUser();
        String newAccessToken = jwtUtil.createAccessToken(user);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "message", "Access Token 재발급 완료"
        ));
    }

    // 소셜 로그인 (Google, Kakao, Naver)
    @PostMapping("/oauth2")
    public ResponseEntity<?> oauth2Callback(@RequestBody AuthRequest requestDto) {
        String provider = requestDto.getProvider(); // google, kakao, naver
        String code = requestDto.getCode();

        try {
            // 1. Authorization Code → access_token, id_token 교환
            Map<String, String> tokenInfo = authService.exchangeCodeForToken(provider, code);
            String idToken = tokenInfo.get("id_token");
            String accessToken = tokenInfo.get("access_token");

            // 2. id_token Payload 출력 (디버깅용)
            JwtUtil.printDecodedToken(idToken);

            // 3. 사용자 정보 추출
            String socialId = JwtUtil.decodeClaim(idToken, "sub");
            String email;

            // 네이버는 id_token에 email이 없으므로 userinfo API 호출
            if ("naver".equalsIgnoreCase(provider)) {
                email = authService.getNaverEmail(accessToken);
            } else {
                email = JwtUtil.decodeClaim(idToken, "email");
            }

            // 4. DB 저장 및 JWT 생성
            Map<String, String> tokens = userService.saveOrLoginUser(
                    null,
                    email,
                    null,
                    null,
                    provider.toUpperCase(),
                    socialId
            );

            return ResponseEntity.ok(Map.of(
                    "accessToken", tokens.get("accessToken"),
                    "refreshToken", tokens.get("refreshToken"),
                    "message", "로그인 성공"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "소셜 로그인 처리 중 오류 발생", "error", e.getMessage()));
        }
    }

    // 로그아웃 (Refresh Token 삭제)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest logoutDto) {
        String refreshToken = logoutDto.getRefreshToken();

        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(refreshTokenRepository::delete);
        }

        return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }
}
