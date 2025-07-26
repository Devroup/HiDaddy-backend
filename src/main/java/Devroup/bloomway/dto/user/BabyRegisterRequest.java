package Devroup.bloomway.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class BabyRegisterRequest {
    private String userName;
    private String babyName;
    private String dueDate; // 예: "2025-12-25"

    public LocalDateTime getParsedDueDate() {
        if (dueDate == null || dueDate.isBlank()) {
            throw new IllegalArgumentException("출산 예정일은 필수입니다.");
        }
        return LocalDate.parse(dueDate).atStartOfDay(); // "yyyy-MM-dd" 형식으로 들어올 거임
    }
}
