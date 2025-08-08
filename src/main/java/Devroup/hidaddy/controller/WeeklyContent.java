package Devroup.hidaddy.controller;

import Devroup.hidaddy.dto.weeklycontent.WeeklyContentResponse;
import Devroup.hidaddy.dto.weeklycontent.WeeklyContentSimpleResponse;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.service.WeeklyContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weekly")
@RequiredArgsConstructor
@Tag(name = "WeeklyContent", description = "주차별 정보 조회 API")
public class WeeklyContent {
    private final WeeklyContentService service;
    private final WeeklyContentService weeklyContentService;

    @Operation(summary = "현재 주차의 정보 조회", description = "아이의 출산 예정일로 현재 주차를 계산하고 정보를 조회합니다.")
    @GetMapping("/current")
    public ResponseEntity<WeeklyContentResponse> getCurrentWeeklyContent(
            @RequestParam Long groupId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(weeklyContentService.getWeeklyContent(groupId));
    }

    @Operation(summary = "특정 주차의 정보 조회", description = "원하는 특정 주차의 정보를 조회합니다.")
    @GetMapping("/{week}")
    public ResponseEntity<WeeklyContentSimpleResponse> getWeeklyContent(
            @PathVariable int week
    ) {
        return ResponseEntity.ok(weeklyContentService.getWeeklyContentSimple(week));
    }
}
