package Devroup.bloomway.controller;

import Devroup.bloomway.dto.user.*;
import Devroup.bloomway.dto.auth.*;
import Devroup.bloomway.entity.RefreshToken;
import Devroup.bloomway.entity.User;
import Devroup.bloomway.jwt.JwtUtil;
import Devroup.bloomway.repository.user.*;
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

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(
            @RequestBody RefreshTokenRequest requestDto
    ) {
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

