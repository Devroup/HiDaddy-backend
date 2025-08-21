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
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;


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
    public ResponseEntity<MissionHistoryResponse> getMissionHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        MissionHistoryResponse missionHistory = missionService.getMissionHistory(userDetails.getUser());
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

        MissionResponse missionDetail = missionService.getMissionDetail(missionId, userDetails.getUser());
        return ResponseEntity.ok(missionDetail);
    }

    @PostMapping("/today")
    @Operation(
            summary = "오늘 미션 조회 또는 생성",
            description = "로그인한 사용자의 오늘 미션을 조회합니다. "
                    + "이미 생성된 미션이 있으면 조회 결과를 반환하고, 없으면 AI를 통해 새 미션을 생성하여 반환합니다. "
                    + "Authorization 헤더에 유효한 Access Token이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "오늘 미션 조회 또는 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
    })
    public ResponseEntity<MissionKeywordResponse> getOrCreateTodayMission(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        MissionKeywordResponse response = missionService.getOrCreateTodayMission(userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{missionId}/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "미션 사진 검증",
            description = "지정된 미션에 대해 사진을 업로드하여 적합 여부를 검증합니다. "
                    + "미션의 설명과 키워드를 기반으로 AI가 사진을 판독해 결과를 반환합니다. "
                    + "사진과 함께 사용자의 속마음(content)도 함께 저장됩니다. "
                    + "Authorization 헤더에 유효한 Access Token이 필요합니다."
)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "미션 검증 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "400", description = "해당 미션을 찾을 수 없음")
    })
    public ResponseEntity<MissionAIResponse> analyzeMission(
        @PathVariable Long missionId,
        @RequestPart(value = "image", required = false) MultipartFile image,
        @RequestPart(value = "content", required = false) String content,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        MissionAIResponse result = missionService.analyzeMissionPhoto(missionId, image, content, userDetails.getUser());
        return ResponseEntity.ok(result);
    }
} 