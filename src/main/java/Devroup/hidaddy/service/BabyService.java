package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.user.BabyRegisterListRequest;
import Devroup.hidaddy.dto.user.BabyRegisterRequest;
import Devroup.hidaddy.dto.user.BabyResponse;
import Devroup.hidaddy.dto.user.BabyGroupResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            LocalDateTime parsedDueDate = dto.getDueDate().atStartOfDay();

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
            LocalDateTime parsedDueDate = dto.getDueDate().atStartOfDay();

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
    @Transactional
    public List<BabyGroupResponse> getBabies(User user) {
        List<Baby> babies = babyRepository.findAllByUser(user);

        return babies.stream()
                .collect(Collectors.groupingBy(b -> b.getBabyGroup().getId()))
                .entrySet()
                .stream()
                .map(entry -> {
                    Long groupId = entry.getKey();
                    List<Baby> groupBabies = entry.getValue();
                    boolean isTwin = groupBabies.size() > 1;

                    // 기준 dueDate는 첫 번째 아기의 것으로 사용
                    LocalDate dueDate = groupBabies.get(0).getDueDate().toLocalDate();

                    List<BabyResponse> babyResponses = groupBabies.stream()
                            .map(b -> new BabyResponse(b, isTwin))
                            .toList();

                    return BabyGroupResponse.builder()
                            .babyGroupId(groupId)
                            .isTwin(isTwin)
                            .dueDate(dueDate)
                            .babies(babyResponses)
                            .build();
                })
                .toList();
    }


    // 아기 그룹 수정
    @Transactional
    public List<BabyResponse> updateBabyGroup(Long groupId, List<BabyRegisterRequest> updates) {
        BabyGroup group = babyGroupRepository.findWithBabiesById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("선택된 아기 그룹을 찾을 수 없습니다."));

        List<Baby> babies = group.getBabies();

        for (int i = 0; i < updates.size(); i++) {
            Baby baby = babies.get(i);
            BabyRegisterRequest dto = updates.get(i);

            if (dto.getBabyName() != null) baby.setName(dto.getBabyName());
            if (dto.getDueDate() != null) baby.setDueDate(dto.getDueDate().atStartOfDay());
        }

        babyRepository.saveAll(babies);

        return convertToResponses(babies, babies.size() > 1);
    }

    @Transactional
    public void deleteBabyGroup(User user, Long groupId) {
        BabyGroup group = babyGroupRepository.findWithBabiesById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("선택된 아기 그룹을 찾을 수 없습니다."));

        babyGroupRepository.delete(group);  // babies도 같이 삭제됨

        // 해당 유저가 가진 다른 아기들 중 하나를 가져오기 (dueDate 기준 최신순)
        List<Baby> remainingBabies = babyRepository.findAllByUser(user);
        if (!remainingBabies.isEmpty()) {
            // 가장 최근 등록된 아기의 babyGroupId를 선택
            Long newSelectedGroupId = remainingBabies.get(0).getBabyGroup().getId();
            user.setSelectedBabyId(newSelectedGroupId);
        } else {
            user.setSelectedBabyId(null);
        }

        userRepository.save(user);
    }

    private List<BabyResponse> convertToResponses(List<Baby> babies, boolean isTwin) {
        return babies.stream()
                .map(b -> new BabyResponse(b, isTwin))
                .toList();
    }
}
