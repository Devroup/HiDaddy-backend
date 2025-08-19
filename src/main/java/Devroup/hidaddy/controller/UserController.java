package Devroup.hidaddy.controller;

import Devroup.hidaddy.dto.user.*;
import Devroup.hidaddy.security.UserDetailsImpl;
import Devroup.hidaddy.service.BabyService;
import Devroup.hidaddy.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(
        name = "User",
        description = "회원 정보, 아기 등록 및 선택 등 사용자 관련 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final BabyService babyService;

    // 현재 로그인한 유저 정보 반환
    @GetMapping
    @Operation(
            summary = "현재 로그인된 유저 정보 조회",
            description = "로그인된 사용자의 이름, 전화번호, 배우자 전화번호, 선택된 아기의 이름을 반환합니다. "
                    + "Authorization 헤더에 유효한 Access Token이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "404", description = "해당 유저를 찾을 수 없음")
    })
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse userInfo = userService.getUserInfo(userDetails.getUser().getId());
        return ResponseEntity.ok(userInfo);
    }

    // 유저 이름 변경
    @PatchMapping("/name")
    @Operation(
            summary = "유저 이름 변경",
            description = "로그인된 사용자의 이름을 새 이름으로 변경합니다. "
                    + "요청 시 JSON 바디로 `userName`을 전달하며, "
                    + "Authorization 헤더에 유효한 Access Token이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 이름 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)")
    })
    public ResponseEntity<MessageResponse> changeName(
            @RequestBody ChangeUserNameRequest requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("인증이 필요합니다."));
        }
        if (requestDto == null || requestDto.getUserName() == null || requestDto.getUserName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("이름은 비워둘 수 없습니다."));
        }

        userService.changeUserName(userDetails.getUser(), requestDto.getUserName().trim());
        return ResponseEntity.ok(new MessageResponse("유저 이름 변경 완료"));
    }

    // 아기 등록 (튜토리얼 플로우)
    @PostMapping("/baby")
    @Operation(summary = "아기 정보 등록 (튜토리얼)",
            description = "로그인한 사용자의 이름과 아기 정보를 등록하고, 선택된 아기 ID도 자동으로 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "아기 정보 등록 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)")
    })
    public ResponseEntity<BabyRegisterResponse> registerBaby(
            @RequestBody BabyRegisterListRequest requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<BabyResponse> babies = babyService.registerBabyGroupTutorial(requestDto, userDetails.getUser());
        BabyRegisterResponse response = new BabyRegisterResponse(requestDto.getUserName(), babies);
        return ResponseEntity.ok(response);
    }

    // 전체 아기 목록 조회
    @GetMapping("/all-babies")
    @Operation(summary = "전체 아기 목록 조회",
            description = "로그인된 사용자의 전체 아기 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "아기 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)")
    })
    public ResponseEntity<List<BabyGroupResponse>> getAllBabies(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<BabyGroupResponse> babies = babyService.getBabies(userDetails.getUser());
        return ResponseEntity.ok(babies);
    }

    // 선택된 아기 정보 조회 (메인/마이페이지 공용)
    @GetMapping("/baby")
    @Operation(summary = "특정 아기 정보 조회 (메인화면 + 마이페이지 공용)",
            description = "선택된 아기의 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "아기 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 아기 없음")
    })
    public ResponseEntity<SelectedBabyResponse> getSelectedBabyInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SelectedBabyResponse selectedBabyInfo = userService.getSelectedBabyInfo(userDetails.getUser());
        return ResponseEntity.ok(selectedBabyInfo);
    }

    // 아기 정보 수정 (이름, 출산 예정일 등)
    @PatchMapping("/baby/{groupId}")
    @Operation(summary = "아기 정보 수정", description = "지정된 아기의 이름, 출산 예정일 등을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)")
    })
    public ResponseEntity<List<BabyResponse>> updateBaby(
            @PathVariable Long groupId,
            @RequestBody List<BabyRegisterRequest> updates,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<BabyResponse> response = babyService.updateBabyGroup(groupId, updates);
        return ResponseEntity.ok(response);
    }

    // 아기 그룹 삭제
    @DeleteMapping("/baby/{groupId}")
    @Operation(summary = "아기 삭제", description = "지정된 아기 정보를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "아기 그룹 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)")
    })
    public ResponseEntity<MessageResponse> deleteBaby(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        babyService.deleteBabyGroup(userDetails.getUser(), groupId);
        return ResponseEntity.ok(new MessageResponse("아기 그룹 삭제 완료"));
    }

    // 간단 아기 등록 (태명/예정일만)
    @PostMapping("/baby/basic")
    @Operation(summary = "아기 정보 등록", description = "태명과 출산 예정일만 입력하여 아기를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "아기 정보 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)")
    })
    public ResponseEntity<List<BabyResponse>> registerBabySimple(
            @RequestBody BabyBasicRegisterListRequest requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<BabyResponse> response = babyService.registerBabyGroup(requestDto.getBabies(), userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    // 선택된 아기 그룹 변경
    @PatchMapping("/select-baby-group/{groupId}")
    @Operation(summary = "선택된 아기 변경",
            description = "로그인한 사용자가 등록한 아기 중 하나를 선택된 아기로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "선택된 아기 변경 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "404", description = "해당 아기 정보를 찾을 수 없음")
    })
    public ResponseEntity<SelectedBabyResponse> selectBabyGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SelectedBabyResponse selectedBabyResponse = userService.changeSelectedBabyGroup(userDetails.getUser(), groupId);
        return ResponseEntity.ok(selectedBabyResponse);
    }

    // 프로필 이미지 업로드
    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "프로필 이미지 업로드",
            description = "로그인된 사용자의 프로필 이미지를 업로드합니다. Authorization 헤더에 유효한 Access Token이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 이미지 업로드 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<ProfileImageResponse> uploadProfileImage(
            @RequestPart(value = "image") MultipartFile image,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String imageUrl = userService.uploadProfileImage(userDetails.getUser(), image);
        return ResponseEntity.ok(new ProfileImageResponse(imageUrl));
    }

    // 사용자/파트너 전화번호 등록 및 수정
    @PatchMapping("/phone")
    @Operation(
            summary = "사용자 및 파트너 전화번호 등록/수정",
            description = "로그인된 사용자의 `phone`과 `partnerPhone`을 등록하거나 수정합니다. "
                    + "요청 시 JSON 바디로 `phone`, `partnerPhone` 값을 전달하며, "
                    + "둘 중 하나만 보내도 되고, 보내지 않은 필드는 기존 값을 유지합니다. "
                    + "Authorization 헤더에 유효한 Access Token이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전화번호 등록/수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (전화번호 형식 오류 등)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "404", description = "해당 유저를 찾을 수 없음")
    })
    public ResponseEntity<MessageResponse> patchPhoneNumbers(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody PhoneUpdateRequest dto
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        userService.updatePhoneNumbers(userDetails.getUser().getId(), dto);
        return ResponseEntity.ok(new MessageResponse("전화번호가 성공적으로 변경되었습니다."));
    }
}
