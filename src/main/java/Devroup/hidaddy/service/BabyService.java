package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.user.BabyBasicRegisterRequest;
import Devroup.hidaddy.dto.user.BabyResponse;
import Devroup.hidaddy.dto.user.BabyUpdateRequest;
import Devroup.hidaddy.entity.Baby;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.repository.user.BabyRepository;
import Devroup.hidaddy.repository.user.UserRepository;
import Devroup.hidaddy.util.S3Uploader;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BabyService {

    private final UserRepository userRepository;
    private final BabyRepository babyRepository;
    private final S3Uploader s3Uploader;

    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

    // 아기 리스트 조회
    public List<BabyResponse> getBabies(User user) {
        List<Baby> babies = babyRepository.findByUser(user);
        return babies.stream()
                .map(BabyResponse::new)
                .toList();
    }

    // 아기 조회
    public BabyResponse getBaby(User user, Long babyId) {
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아기를 찾을 수 없습니다."));
        if (!baby.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 아기에 대한 접근 권한이 없습니다.");
        }
        return new BabyResponse(baby);
    }

    // 아기 수정
    @Transactional
    public BabyResponse updateBaby(User user, Long babyId, BabyUpdateRequest dto, MultipartFile image) {
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아기를 찾을 수 없습니다."));

        if (!baby.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 아기에 대한 수정 권한이 없습니다.");
        }

        if (dto.getName() != null) baby.setName(dto.getName());
        if (dto.getDueDate() != null) baby.setDueDate(dto.getDueDate().atStartOfDay());

        if (image != null && !image.isEmpty()) {
            // 기존 이미지 삭제
            if (baby.getBabyImageUrl() != null && !baby.getBabyImageUrl().isEmpty()) {
                String imageKey = baby.getBabyImageUrl().replace(cloudFrontDomain + "/", "");
                s3Uploader.delete(imageKey);
            }

            // 새 이미지 업로드
            String imageUrl = s3Uploader.upload(image, "baby-profile");
            imageUrl = cloudFrontDomain + "/" + imageUrl;
            baby.setBabyImageUrl(imageUrl);
        }

        return new BabyResponse(babyRepository.save(baby));
    }

    // 아기 삭제
    public void deleteBaby(User user, Long babyId) {
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아기를 찾을 수 없습니다."));
        if (!baby.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 아기에 대한 삭제 권한이 없습니다.");
        }
        babyRepository.delete(baby);
    }

    @Transactional
    public BabyResponse registerBabyBasic(BabyBasicRegisterRequest request, User user) {
        // 아기 생성
        Baby baby = Baby.builder()
                .name(request.getBabyName())
                .dueDate(request.getParsedDueDate())
                .user(user)
                .build();

        // 저장
        babyRepository.save(baby);

        // 선택된 아기로 지정
        user.setSelectedBabyId(baby.getId());
        userRepository.save(user);

        return new BabyResponse(baby);
    }
}
