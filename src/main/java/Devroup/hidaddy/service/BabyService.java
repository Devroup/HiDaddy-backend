package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.user.BabyRegisterListRequest;
import Devroup.hidaddy.dto.user.BabyRegisterRequest;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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

    // 아기 수정
    @Transactional
    public BabyResponse updateBaby(User user, Long babyId, BabyUpdateRequest dto) {
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아기를 찾을 수 없습니다."));

        if (!baby.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 아기에 대한 수정 권한이 없습니다.");
        }

        if (dto.getName() != null) baby.setName(dto.getName());
        if (dto.getDueDate() != null) baby.setDueDate(dto.getDueDate().atStartOfDay());

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

    public List<BabyResponse> registerBabyBasic(List<BabyRegisterRequest> babies, User user) {

        List<BabyResponse> responses = new ArrayList<>();

        for (int i = 0; i < babies.size(); i++) {
            BabyRegisterRequest dto = babies.get(i);
            LocalDateTime parsedDueDate = parseDueDate(dto.getDueDate());

            Baby baby = Baby.builder()
                    .name(dto.getBabyName())
                    .dueDate(parsedDueDate)
                    .user(user)
                    .build();

            babyRepository.save(baby);
            responses.add(new BabyResponse(baby));

            // 첫 번째 아기를 선택된 아기로 설정
            if (i == 0) {
                user.setSelectedBabyId(baby.getId());
            }
        }

        userRepository.save(user); // 변경된 selectedBabyId 저장

        return responses;
    }

    public List<BabyResponse> registerBaby(BabyRegisterListRequest request, User user) {
        List<BabyRegisterRequest> babyList = request.getBabies();

        if (babyList == null || babyList.isEmpty()) {
            throw new IllegalArgumentException("최소 한 명 이상의 아기 정보가 필요합니다.");
        }

        // 유저 이름 설정
        user.setName(request.getUserName());

        List<BabyResponse> responses = new ArrayList<>();

        for (int i = 0; i < babyList.size(); i++) {
            BabyRegisterRequest dto = babyList.get(i);
            LocalDateTime parsedDueDate = parseDueDate(dto.getDueDate());

            Baby baby = Baby.builder()
                    .name(dto.getBabyName())
                    .dueDate(parsedDueDate)
                    .user(user)
                    .build();

            babyRepository.save(baby);
            responses.add(new BabyResponse(baby));

            // 첫 번째 아기를 선택된 아기로 지정
            if (i == 0) {
                user.setSelectedBabyId(baby.getId());
            }
        }

        userRepository.save(user); // 선택된 아기 ID 저장

        return responses;
    }

    private LocalDateTime parseDueDate(String dueDate) {
        try {
            return LocalDate.parse(dueDate).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("출산 예정일 형식이 올바르지 않습니다. yyyy-MM-dd 형식이어야 합니다.");
        }
    }
}
