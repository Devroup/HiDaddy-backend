package Devroup.bloomway.service;

import Devroup.bloomway.entity.User;
import Devroup.bloomway.entity.RefreshToken;
import Devroup.bloomway.repository.UserRepository;
import Devroup.bloomway.repository.RefreshTokenRepository;
import Devroup.bloomway.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public Map<String, String> saveOrLoginUser(String name, String email, String phone,
                                               String partnerPhone, String loginType, String socialId) {

        // 유저 조회 또는 생성
        User user = userRepository.findBySocialIdAndLoginType(socialId, loginType)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setPhone(phone);
                    newUser.setPartnerPhone(partnerPhone);
                    newUser.setLoginType(loginType);
                    newUser.setSocialId(socialId);
                    return userRepository.save(newUser);
                });

        // Access + Refresh Token 생성
        String accessToken = jwtUtil.createAccessToken(user);
        String refreshTokenStr = jwtUtil.createRefreshToken();
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(14);

        // RefreshToken DB에 저장 (기존 존재 여부에 따라 분기)
        RefreshToken existing = refreshTokenRepository.findByUser(user).orElse(null);
        if (existing != null) {
            existing.updateToken(refreshTokenStr, expiredAt);
            refreshTokenRepository.save(existing);
        } else {
            RefreshToken refreshToken = new RefreshToken(refreshTokenStr, user, expiredAt);
            refreshTokenRepository.save(refreshToken);
        }

        // 응답으로 전달할 토큰들
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        // tokens.put("refreshToken", refreshTokenStr); // 필요 시 제거 가능
        return tokens;
    }
}
