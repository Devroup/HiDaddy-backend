package Devroup.hidaddy.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "기본 메시지 응답 DTO")
public class ApiMessageResponse {

    private String message;
}
