package Devroup.hidaddy.controller;

import Devroup.hidaddy.dto.message.MessageResponse;
import Devroup.hidaddy.service.MessageService;
import Devroup.hidaddy.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Message",
        description = "메시지 전송 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("")
    @Operation(
            summary = "SMS 메시지 전송",
            description = "로그인된 사용자의 배우자 번호로 SMS 메시지를 전송합니다. "
                    + "Authorization 헤더에 유효한 Access Token이 필요합니다. <br>"
                    + "무료 토큰이 15개여서, 다른 플랫폼으로 내부적으로 변경 예정입니다. 해당 API는 유지됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 전송 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "400", description = "배우자 번호가 없거나 잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<MessageResponse> sendSms(
            @RequestParam String text,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        MessageResponse response = messageService.sendSmsToPartner(userDetails.getUser(), text);
        return ResponseEntity.ok(response);
    }
}