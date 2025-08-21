package Devroup.hidaddy.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class PhoneUpdateRequest {
    @Schema(description = "와이프 전화번호 (000-0000-0000 형식)", example = "010-1234-5678")
    private String partnerPhone;
}