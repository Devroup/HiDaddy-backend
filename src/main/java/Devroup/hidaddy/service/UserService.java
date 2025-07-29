package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.user.*;
import Devroup.hidaddy.entity.Baby;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.entity.RefreshToken;
import Devroup.hidaddy.repository.user.*;
import Devroup.hidaddy.repository.auth.*;
import Devroup.hidaddy.jwt.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BabyRepository babyRepository;

        public void registerBaby(BabyRegisterRequest dto, User user) {
        // 1. 유저 이름 업데이트
        user.setName(dto.getUserName());

        // 2. dueDate 파싱
        LocalDateTime dueDate = dto.getParsedDueDate();

        // 3. 아기 생성
        Baby baby = Baby.builder()
                .name(dto.getBabyName())
                .dueDate(dueDate)
                .user(user)
                .build();

        babyRepository.save(baby);

        // 4. 선택된 아기 ID 설정
        user.setSelectedBabyId(baby.getId());
        userRepository.save(user);
    }

    public void changeSelectedBaby(User user, Long babyId) {
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아기를 찾을 수 없습니다."));

        // 본인의 아기인지 확인
        if (!baby.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 아기만 선택할 수 있습니다.");
        }

        user.setSelectedBabyId(babyId);
        userRepository.save(user);
    }

    public void changeUserName(User user, String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 비워둘 수 없습니다.");
        }
        user.setName(userName);
        userRepository.save(user);
    }

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
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(365);

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
        tokens.put("refreshToken", refreshTokenStr);
        return tokens;
    }

    @Transactional
    public void deleteUser(User user) {
        // RefreshToken 먼저 삭제
        refreshTokenRepository.deleteByUser(user);

        // 아기 정보 삭제 (Cascade 걸려 있지 않다면 명시적 삭제 필요)
        babyRepository.deleteAllByUser(user);

        // 사용자 삭제
        userRepository.delete(user);
    }
}
