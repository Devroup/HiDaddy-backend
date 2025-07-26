package Devroup.bloomway.controller;

import Devroup.bloomway.dto.user.*;
import Devroup.bloomway.security.UserDetailsImpl;
import Devroup.bloomway.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/select-baby/{babyId}")
    public ResponseEntity<?> selectBaby(@PathVariable Long babyId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        userService.changeSelectedBaby(userDetails.getUser(), babyId);
        return ResponseEntity.ok("선택된 아기 변경 완료");
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdraw(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.deleteUser(userDetails.getUser());
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
}
