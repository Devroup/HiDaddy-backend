package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.user.*;
import Devroup.hidaddy.entity.Baby;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.entity.RefreshToken;
import Devroup.hidaddy.repository.user.*;
import Devroup.hidaddy.repository.auth.*;
import Devroup.hidaddy.jwt.JwtUtil;
import Devroup.hidaddy.util.S3Uploader;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BabyRepository babyRepository;
    private final S3Uploader s3Uploader;

    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

    public void registerBaby(BabyRegisterRequest dto, User user) {
        // 유저 이름 업데이트
        user.setName(dto.getUserName());

        // 아기 생성
        Baby baby = Baby.builder()
                .name(dto.getBabyName())
                .dueDate(dto.getParsedDueDate())
                .user(user)
                .build();

        babyRepository.save(baby);

        // 선택된 아기 ID 설정
        user.setSelectedBabyId(baby.getId());
        userRepository.save(user);
    }

    public BabyResponse changeSelectedBaby(User user, Long babyId) {
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아기를 찾을 수 없습니다."));

        // 본인의 아기인지 확인
        if (!baby.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 아기만 선택할 수 있습니다.");
        }

        user.setSelectedBabyId(babyId);
        userRepository.save(user);

        return new BabyResponse(baby);
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
                    newUser.setProfileImageUrl(cloudFrontDomain + "/profile/default_image.svg"); // 기본 프로필 이미지 URL 설정
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

    public UserResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String babyName = null;
        if (user.getSelectedBabyId() != null) {
            babyName = babyRepository.findById(user.getSelectedBabyId())
                    .map(Baby::getName)
                    .orElse(null);
        }

        return new UserResponse(user, babyName);
    }

    public SelectedBabyResponse getSelectedBabyInfo(User currentUser) {
        Long selectedBabyId = currentUser.getSelectedBabyId();
        if(selectedBabyId == null)
            throw new IllegalArgumentException("선택된 아이가 없습니다.");

        Baby baby = babyRepository.findByIdAndUserId(
                        currentUser.getSelectedBabyId(),
                        currentUser.getId()
                )
                .orElseThrow(() -> new IllegalArgumentException("해당 아이를 찾을 수 없습니다."));

        return SelectedBabyResponse.from(baby);
    }

    @Transactional
    public String uploadProfileImage(User user, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 필요합니다.");
        }

        // 기존 프로필 이미지가 있다면 S3에서 삭제
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            String imageKey = user.getProfileImageUrl().replace(cloudFrontDomain + "/", "");
            s3Uploader.delete(imageKey);
        }

        // 새 이미지를 S3에 업로드
        String imageUrl = s3Uploader.upload(image, "profile");
        imageUrl = cloudFrontDomain + "/" + imageUrl;

        // 사용자의 프로필 이미지 URL 업데이트
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }

    public void updatePhoneNumbers(Long userId, PhoneUpdateRequest dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getPartnerPhone() != null) {
            user.setPartnerPhone(dto.getPartnerPhone());
        }

        userRepository.save(user);
    }
}
