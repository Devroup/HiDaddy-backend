package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.user.*;
import Devroup.hidaddy.entity.Baby;
import Devroup.hidaddy.entity.BabyComment;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BabyRepository babyRepository;
    private final S3Uploader s3Uploader;
    private final BabyCommentRepository babyCommentRepository;
    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

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

    public Map<String, Object> saveOrLoginUser(String name, String email, String phone,
                                               String partnerPhone, String loginType, String socialId) {

        AtomicBoolean isNewUser = new AtomicBoolean(false);

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
                    newUser.setProfileImageUrl(cloudFrontDomain + "/profile/default_image.svg");
                    isNewUser.set(true);
                    return userRepository.save(newUser);
                });

        // Access + Refresh Token 생성
        String accessToken = jwtUtil.createAccessToken(user);
        String refreshTokenStr = jwtUtil.createRefreshToken();
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(365);

        // RefreshToken DB 저장 또는 갱신
        RefreshToken existing = refreshTokenRepository.findByUser(user).orElse(null);
        if (existing != null) {
            existing.updateToken(refreshTokenStr, expiredAt);
            refreshTokenRepository.save(existing);
        } else {
            RefreshToken refreshToken = new RefreshToken(refreshTokenStr, user, expiredAt);
            refreshTokenRepository.save(refreshToken);
        }

        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshTokenStr);
        response.put("isNewUser", isNewUser.get()); // true = 처음 가입한 유저

        return response;
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
        
        int currentWeek = calculateCurrentWeek(baby.getDueDate().toLocalDate());

        // 주차별 코멘트 조회
        String comment = babyCommentRepository
                .findByWeekStartLessThanEqualAndWeekEndGreaterThanEqual(currentWeek, currentWeek)
                .map(BabyComment::getComment)
                .orElse("등록된 코멘트가 없습니다.");

        return SelectedBabyResponse.from(baby, comment);
    }

    // 현재 주차 계산 (예정일 기준)
    private int calculateCurrentWeek(LocalDate dueDate) {
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        int weeksUntilDue = (int) Math.ceil(daysUntilDue / 7.0);  // 남은 주수
        return 40 - weeksUntilDue; // 임신 40주 기준 현재 주차
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
