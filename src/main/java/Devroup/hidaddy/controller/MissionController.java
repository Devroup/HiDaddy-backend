package Devroup.hidaddy.controller;

import Devroup.hidaddy.dto.mission.*;
import Devroup.hidaddy.security.UserDetailsImpl;
import Devroup.hidaddy.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(
        name = "Mission",
        description = "미션 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mission")
public class MissionController {

    private final MissionService missionService;

    @GetMapping("/keywords")
    public ResponseEntity<MissionKeywordResponse> generateTodayMission(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        List<String> keywords = missionService.generateMissionForToday(userDetails.getUser());
        return ResponseEntity.ok(new MissionKeywordResponse(keywords));
    }


    @GetMapping("")
    @Operation(
            summary = "미션 과거목록 조회",
            description = "로그인된 사용자의 미션 과거 목록을 조회합니다. "
                    + "Authorization 헤더에 유효한 Access Token이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "미션 과거목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)")
    })
    public ResponseEntity<List<MissionLogResponse>> getMissionHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        List<MissionLogResponse> missionHistory = missionService.getMissionHistory(userDetails.getUser());
        return ResponseEntity.ok(missionHistory);
    }

    @GetMapping("/{missionId}")
    @Operation(
            summary = "미션 과거 상세 단건 조회",
            description = "특정 미션의 상세 정보를 조회합니다. "
                    + "Authorization 헤더에 유효한 Access Token이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "미션 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "400", description = "해당 미션을 찾을 수 없음")
    })
    public ResponseEntity<MissionResponse> getMissionDetail(
            @PathVariable Long missionId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        MissionResponse missionDetail = missionService.getMissionDetail(missionId);
        return ResponseEntity.ok(missionDetail);
    }
} 