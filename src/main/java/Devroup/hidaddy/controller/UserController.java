package Devroup.hidaddy.controller;

import Devroup.hidaddy.dto.user.*;
import Devroup.hidaddy.security.UserDetailsImpl;
import Devroup.hidaddy.service.UserService;
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
        description = "회원 정보, 아기 등록 및 선택 등 사용자 관련 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    /*
    @GetMapping("/baby")
    public String babyRegisterPage(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return "<html><body><h2>인증되지 않은 사용자입니다.</h2></body></html>";
        }

        User user = userDetails.getUser();
        String html = """
        <html>
        <head><title>아기 정보 입력</title></head>
        <body>
            <h2>추가 정보 입력</h2>
            <form method=\"post\" action=\"/api/user/baby\">
                <label>이름: <input type=\"text\" name=\"userName\" /></label><br/>
                <label>아기 태명: <input type=\"text\" name=\"babyName\" /></label><br/>
                <label>출산 예정일: <input type=\"date\" name=\"dueDate\" /></label><br/>
                <button type=\"submit\">등록하기</button>
            </form>
        </body>
        </html>
        """;
        return html;
    }
    */

    @Operation(summary = "아기 정보 등록",
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

    @Operation(summary = "선택된 아기 변경",
            description = "로그인한 사용자가 등록한 아기 중 하나를 선택된 아기로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "선택된 아기 변경 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "404", description = "해당 아기 정보를 찾을 수 없음")
    })
    @PatchMapping("/select-baby/{babyId}")
    public ResponseEntity<?> selectBaby(@PathVariable Long babyId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        userService.changeSelectedBaby(userDetails.getUser(), babyId);
        return ResponseEntity.ok("선택된 아기 변경 완료");
    }


    @PatchMapping("/change-name")
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

    @GetMapping("/me")
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

}
