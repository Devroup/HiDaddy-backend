package Devroup.hidaddy.controller;

import Devroup.hidaddy.dto.auth.*;
import Devroup.hidaddy.dto.user.MessageResponse;
import Devroup.hidaddy.entity.RefreshToken;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.jwt.JwtUtil;
import Devroup.hidaddy.repository.auth.RefreshTokenRepository;
import Devroup.hidaddy.repository.user.UserRepository;
import Devroup.hidaddy.security.UserDetailsImpl;
import Devroup.hidaddy.service.AuthService;
import Devroup.hidaddy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
        name = "Authentication",
        description = "Access/Refresh Token 발급 및 갱신, 로그아웃, 회원탈퇴 등 인증 관련 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final AuthService authService;
    private final UserRepository userRepository;

    // Refresh Token을 검증하고, 유효하면 새로운 Access Token을 발급
    @Operation(
            summary = "Access Token 재발급",
            description = "만료된 Access Token을 Refresh Token을 사용하여 새로 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access Token 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Refresh Token")
    })
    @PostMapping("/renew")
    public ResponseEntity<RenewResponse> refreshAccessToken(
            @Parameter(description = "리프레시 토큰 요청 DTO")
            @RequestBody RefreshTokenRequest requestDto) {

        String refreshToken = requestDto.getRefreshToken();
        RefreshToken foundToken = refreshTokenRepository.findByToken(refreshToken).orElse(null);

        if (foundToken == null || foundToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findById(foundToken.getUser().getId()).orElseThrow();
        String newAccessToken = jwtUtil.createAccessToken(user);

        RenewResponse response = new RenewResponse(newAccessToken, "Access Token 재발급 완료");
        return ResponseEntity.ok(response);
    }

    // OAuth2 Provider(구글/카카오/네이버)에서 받은 Authorization Code로 토큰을 교환하고
    // 신규 유저면 회원가입 처리, 기존 유저면 로그인 처리 후 Access/Refresh Token을 발급
    @Operation(
            summary = "소셜 로그인 처리 (Access/Refresh Token 전달)",
            description = "구글, 카카오, 네이버 OAuth 2.0 인증 코드로 자체 Access/Refresh Token을 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "토큰 교환 실패 또는 Provider 오류")
    })
    @PostMapping("/tokens")
    public ResponseEntity<AuthResponse> oauth2Callback(
            @Parameter(description = "SNS 제공자(google, kakao, naver)와 Authorization Code")
            @RequestBody AuthRequest requestDto) {

        if (requestDto == null || requestDto.getProvider() == null || requestDto.getProvider().isBlank()
                || requestDto.getCode() == null || requestDto.getCode().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String provider = requestDto.getProvider();
        String code = requestDto.getCode();

        try {
            Map<String, String> tokenInfo = authService.exchangeCodeForToken(provider, code);
            String idToken = tokenInfo.get("id_token");
            String accessToken = tokenInfo.get("access_token");

            JwtUtil.printDecodedToken(idToken);  // 디버깅용 로그

            String socialId = JwtUtil.decodeClaim(idToken, "sub");
            String email = "naver".equalsIgnoreCase(provider)
                    ? authService.getNaverEmail(accessToken)
                    : JwtUtil.decodeClaim(idToken, "email");

            Map<String, Object> tokens = userService.saveOrLoginUser(
                    null, email, null, null, provider.toUpperCase(), socialId
            );

            AuthResponse response = new AuthResponse(
                    (String) tokens.get("accessToken"),
                    (String) tokens.get("refreshToken"),
                    !(Boolean) tokens.get("isNewUser"),
                    "로그인 성공"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 전달받은 Refresh Token을 DB에서 삭제하여 해당 세션을 무효화
    @Operation(
            summary = "로그아웃",
            description = "사용자의 Refresh Token을 삭제하여 세션을 무효화합니다."
    )
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @Parameter(description = "로그아웃할 사용자의 Refresh Token")
            @RequestBody LogoutRequest logoutDto) {
        String refreshToken = (logoutDto != null) ? logoutDto.getRefreshToken() : null;

        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(refreshTokenRepository::delete);
        }

        return ResponseEntity.ok(new MessageResponse("로그아웃 완료"));
    }

    // 현재 로그인한 사용자의 계정 및 관련 데이터(아기 그룹, Refresh Token 등)를 모두 삭제
    @Operation(summary = "회원 탈퇴",
            description = "로그인한 사용자의 모든 정보와 관련 데이터(아기 정보, Refresh Token 등)를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)")
    })
    @DeleteMapping("/withdraw")
    public ResponseEntity<MessageResponse> withdraw(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("인증되지 않은 사용자입니다."));
        }
        userService.deleteUser(userDetails.getUser());
        return ResponseEntity.ok(new MessageResponse("회원 탈퇴가 완료되었습니다."));
    }
}
