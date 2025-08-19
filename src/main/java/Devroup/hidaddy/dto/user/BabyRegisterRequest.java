package Devroup.hidaddy.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class BabyRegisterRequest {
    private String babyName;

    @Schema(description = "출산 예정일 (yyyy-MM-dd 형식)", example = "2025-12-25")
    private LocalDate dueDate; // 예: "2025-12-25"
}
