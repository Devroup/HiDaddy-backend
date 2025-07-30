package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.mission.MissionLogResponse;
import Devroup.hidaddy.dto.mission.MissionResponse;
import Devroup.hidaddy.entity.Mission;
import Devroup.hidaddy.entity.MissionLog;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.repository.mission.MissionLogRepository;
import Devroup.hidaddy.repository.mission.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionService {

    private final MissionRepository missionRepository;
    private final MissionLogRepository missionLogRepository;

    public List<MissionLogResponse> getMissionHistory(User user) {
        List<MissionLog> missionLogs = missionLogRepository.findByUserOrderByCreatedAtDesc(user);
        return missionLogs.stream()
                .map(MissionLogResponse::from)
                .collect(Collectors.toList());
    }

    public MissionResponse getMissionDetail(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션을 찾을 수 없습니다."));
        return MissionResponse.from(mission);
    }
} 