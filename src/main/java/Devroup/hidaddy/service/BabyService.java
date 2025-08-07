package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.user.BabyRegisterListRequest;
import Devroup.hidaddy.dto.user.BabyRegisterRequest;
import Devroup.hidaddy.dto.user.BabyResponse;
import Devroup.hidaddy.dto.user.BabyUpdateRequest;
import Devroup.hidaddy.entity.Baby;
import Devroup.hidaddy.entity.BabyGroup;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.repository.user.BabyGroupRepository;
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
    private final BabyGroupRepository babyGroupRepository;
    private final S3Uploader s3Uploader;

    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

    // 튜토리얼 단계에서 아기 그룹 등록 (User 이름까지 함께 등록)
    public List<BabyResponse> registerBabyGroupTutorial(BabyRegisterListRequest request, User user) {
        List<BabyRegisterRequest> babies = request.getBabies();
        if (babies == null || babies.isEmpty()) {
            throw new IllegalArgumentException("최소 한 명 이상의 아기 정보가 필요합니다.");
        }

        user.setName(request.getUserName());
        userRepository.save(user);

        BabyGroup babyGroup = new BabyGroup();
        babyGroupRepository.save(babyGroup);

        List<Baby> babyEntities = new ArrayList<>();
        for (BabyRegisterRequest dto : babies) {
            LocalDateTime parsedDueDate = parseDueDate(dto.getDueDate());

            Baby baby = Baby.builder()
                    .name(dto.getBabyName())
                    .dueDate(parsedDueDate)
                    .user(user)
                    .babyGroup(babyGroup)
                    .build();

            babyEntities.add(baby);
        }

        babyRepository.saveAll(babyEntities);
        user.setSelectedBabyId(babyGroup.getId());
        userRepository.save(user);

        return convertToResponses(babyEntities, babyEntities.size() > 1);
    }

    // 아기 그룹 등록 (1명 or 2명 모두 이 메서드 사용)
    public List<BabyResponse> registerBabyGroup(List<BabyRegisterRequest> babies, User user) {
        if (babies == null || babies.isEmpty()) {
            throw new IllegalArgumentException("최소 한 명 이상의 아기 정보가 필요합니다.");
        }

        BabyGroup babyGroup = new BabyGroup();
        babyGroupRepository.save(babyGroup);

        List<Baby> babyEntities = new ArrayList<>();
        for (BabyRegisterRequest dto : babies) {
            LocalDateTime parsedDueDate = parseDueDate(dto.getDueDate());

            Baby baby = Baby.builder()
                    .name(dto.getBabyName())
                    .dueDate(parsedDueDate)
                    .user(user)
                    .babyGroup(babyGroup)
                    .build();

            babyEntities.add(baby);
        }

        babyRepository.saveAll(babyEntities);
        user.setSelectedBabyId(babyGroup.getId());
        userRepository.save(user);

        return convertToResponses(babyEntities, babyEntities.size() > 1);
    }

    // 아기 그룹 전체 조회
    public List<BabyResponse> getBabies(User user) {
        List<Baby> babies = babyRepository.findAllByUser(user);

        return babies.stream()
                .map(baby -> {
                    int groupSize = baby.getBabyGroup().getBabies().size(); // 해당 그룹 내 아기 수
                    boolean isTwin = groupSize > 1;
                    return new BabyResponse(baby, isTwin);
                })
                .toList();
    }


    // 아기 그룹 수정
    @Transactional
    public List<BabyResponse> updateBabyGroup(Long groupId, List<BabyUpdateRequest> updates) {
        BabyGroup group = babyGroupRepository.findWithBabiesById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("선택된 아기 그룹을 찾을 수 없습니다."));

        List<Baby> babies = group.getBabies();

        for (int i = 0; i < updates.size(); i++) {
            Baby baby = babies.get(i);
            BabyUpdateRequest dto = updates.get(i);

            if (dto.getName() != null) baby.setName(dto.getName());
            if (dto.getDueDate() != null) baby.setDueDate(dto.getDueDate().atStartOfDay());
        }

        babyRepository.saveAll(babies);

        return convertToResponses(babies, babies.size() > 1);
    }

    // 아기 그룹 삭제

    public void deleteBabyGroup(User user, Long groupId) {
        BabyGroup group = babyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("선택된 아기 그룹을 찾을 수 없습니다."));

        babyRepository.deleteAll(group.getBabies());
        babyGroupRepository.delete(group);

        user.setSelectedBabyId(null);
        userRepository.save(user);
    }

    private LocalDateTime parseDueDate(String dueDate) {
        try {
            return LocalDate.parse(dueDate).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("출산 예정일 형식이 올바르지 않습니다. yyyy-MM-dd 형식이어야 합니다.");
        }
    }

    private List<BabyResponse> convertToResponses(List<Baby> babies, boolean isTwin) {
        return babies.stream()
                .map(b -> new BabyResponse(b, isTwin))
                .toList();
    }
}
