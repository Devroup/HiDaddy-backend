package Devroup.hidaddy.dto.user;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class BabyBasicRegisterRequest {

    private String babyName;       // 태명
    private String dueDate;        // 예: "2025-12-25" (yyyy-MM-dd 형식)

    public LocalDateTime getParsedDueDate() {
        if (dueDate == null || dueDate.isBlank()) {
            throw new IllegalArgumentException("출산 예정일은 필수입니다.");
        }
        return LocalDate.parse(dueDate).atStartOfDay();
    }
}
