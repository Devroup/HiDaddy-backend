package Devroup.bloomway.controller;

import Devroup.bloomway.entity.RefreshToken;
import Devroup.bloomway.entity.User;
import Devroup.bloomway.jwt.JwtUtil;
import Devroup.bloomway.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        // refreshToken 쿠키가 없음
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("리프레시 토큰 없음");
        }

        // DB에서 refreshToken 조회
        RefreshToken foundToken = refreshTokenRepository.findByToken(refreshToken).orElse(null);

        if (foundToken == null || foundToken.getExpiredAt().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않거나 만료된 리프레시 토큰");
        }

        User user = foundToken.getUser();

        // access token 재발급
        String newAccessToken = jwtUtil.createAccessToken(user);

        // access token 쿠키로 내려주기
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());

        return ResponseEntity.ok(Map.of("message", "Access Token 재발급 완료"));
    }
}
