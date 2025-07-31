package Devroup.hidaddy.controller;

import Devroup.hidaddy.dto.user.*;
import Devroup.hidaddy.entity.Baby;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.security.UserDetailsImpl;
import Devroup.hidaddy.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
        name = "User",
        description = "회원 정보, 사용자, 아기 관련 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("")
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
            return ResponseEntity.status(401).build();
        }

        UserResponse userInfo = userService.getUserInfo(userDetails.getUser().getId());
        return ResponseEntity.ok(userInfo);
    }

    @PatchMapping("/name")
    @Operation(
            summary = "유저 이름 변경",
            description = "로그인된 사용자의 이름을 새 이름으로 변경합니다. "
                    + "요청 시 JSON 바디로 `userName`을 전달하며, "
                    + "Authorization 헤더에 유효한 Access Token이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 이름 변경 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)")
    })
    public ResponseEntity<?> changeName(
            @RequestBody ChangeUserNameRequest requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        userService.changeUserName(userDetails.getUser(), requestDto.getUserName());
        return ResponseEntity.ok("유저 이름 변경 완료");
    }

    @Operation(summary = "아기 정보 등록 (튜토리얼)",
            description = "로그인한 사용자의 이름과 아기 정보를 등록하고, 선택된 아기 ID도 자동으로 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "아기 정보 등록 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)")
    })
    @PostMapping("/baby")
    public ResponseEntity<?> registerBaby(
            @RequestBody BabyRegisterRequest requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        userService.registerBaby(requestDto, userDetails.getUser());
        return ResponseEntity.ok("아기 정보 등록 완료");
    }

    @GetMapping("/baby")
    @Operation(
            summary = "선택된 아기 정보 조회",
            description = "로그인된 사용자가 선택한 아기의 상세 정보(이름, D-day, 프로필 이미지, 메시지)를 반환합니다. "
                    + "Authorization 헤더에 유효한 Access Token이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "선택된 아기 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "400", description = "선택된 아기가 없거나 해당 아기를 찾을 수 없음")
    })
    public ResponseEntity<SelectedBabyResponse> getSelectedBabyInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        SelectedBabyResponse selectedBabyInfo = userService.getSelectedBabyInfo(userDetails.getUser());
        return ResponseEntity.ok(selectedBabyInfo);
    }

    @PatchMapping("/selected-baby/{babyId}")
    @Operation(summary = "선택된 아기 변경",
            description = "로그인한 사용자가 등록한 아기 중 하나를 선택된 아기로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "선택된 아기 변경 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "404", description = "해당 아기 정보를 찾을 수 없음")
    })
    public ResponseEntity<?> selectBaby(@PathVariable Long babyId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        Baby selectedBaby = userService.changeSelectedBaby(userDetails.getUser(), babyId);
        return ResponseEntity.ok(new BabyResponse(selectedBaby));
    }

    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "프로필 이미지 업로드",
            description = "로그인된 사용자의 프로필 이미지를 업로드합니다. "
                    + "Authorization 헤더에 유효한 Access Token이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 이미지 업로드 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<String> uploadProfileImage(
            @RequestPart(value = "image", required = true) MultipartFile image,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        String imageUrl = userService.uploadProfileImage(userDetails.getUser(), image);
        return ResponseEntity.ok(imageUrl);
    }

    @Operation(
            summary = "사용자 및 파트너 전화번호 등록/수정",
            description = "로그인된 사용자의 `phone`과 `partnerphonee`을 등록하거나 수정합니다. "
                    + "요청 시 JSON 바디로 `phone`, `partnerphone` 값을 전달하며, "
                    + "둘 중 하나만 보내도 되고, 보내지 않은 필드는 기존 값을 유지합니다. "
                    + "Authorization 헤더에 유효한 Access Token이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전화번호 등록/수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (전화번호 형식 오류 등)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "404", description = "해당 유저를 찾을 수 없음")
    })
    @PatchMapping("/phone")
    public ResponseEntity<User> patchPhoneNumbers(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @RequestBody PhoneUpdateRequest dto) {
        User updated = userService.updatePhoneNumbers(userDetails.getUser().getId(), dto);
        return ResponseEntity.ok(updated);
    }
}
