package Devroup.hidaddy.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문자 발송 응답 DTO")
public record MessageResponse(
        @Schema(description = "CoolSMS messageId") String messageId,
        @Schema(description = "요청 상태 코드") String status,
        @Schema(description = "수신자 번호") String to
) {}
