package Devroup.bloomway.controller;

import Devroup.bloomway.dto.AuthRequestDto;
import Devroup.bloomway.dto.RefreshTokenRequestDto;
import Devroup.bloomway.entity.RefreshToken;
import Devroup.bloomway.entity.User;
import Devroup.bloomway.jwt.JwtUtil;
import Devroup.bloomway.repository.RefreshTokenRepository;
import Devroup.bloomway.service.AuthService;
import Devroup.bloomway.service.UserService;
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

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshTokenRequestDto requestDto) {
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

    @PostMapping("/oauth2")
    public ResponseEntity<?> oauth2Callback(@RequestBody AuthRequestDto requestDto) {
        String provider = requestDto.getProvider(); // google, kakao, naver
        String code = requestDto.getCode();

        try {
            Map<String, String> tokenInfo = authService.exchangeCodeForToken(provider, code);
            String idToken = tokenInfo.get("id_token");
            String accessToken = tokenInfo.get("access_token");

            // 디코드된 idToken payload 출력
            JwtUtil.printDecodedToken(idToken);

            String socialId = JwtUtil.decodeClaim(idToken, "sub");
            String email;

            // 네이버만 access_token으로 이메일 가져오기
            if ("naver".equalsIgnoreCase(provider)) {
                email = authService.getNaverEmail(accessToken);
            } else {
                email = JwtUtil.decodeClaim(idToken, "email");
            }

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
}
