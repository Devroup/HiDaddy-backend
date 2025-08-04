package Devroup.hidaddy.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class BabyRegisterRequest {
    private String userName;
    private String babyName;

    @Schema(description = "출산 예정일 (yyyy-MM-dd 형식)", example = "2025-12-25")
    private String dueDate; // 예: "2025-12-25"
}
