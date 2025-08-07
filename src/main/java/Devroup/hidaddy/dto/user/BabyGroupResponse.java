package Devroup.hidaddy.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class BabyGroupResponse {
    private Long babyGroupId;
    private boolean isTwin;
    private LocalDate dueDate;  // <-- 추가
    private List<BabyResponse> babies;
}