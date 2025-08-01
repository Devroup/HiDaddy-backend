package Devroup.hidaddy.controller;

import Devroup.hidaddy.dto.weeklycontent.WeeklyContentResponse;
import Devroup.hidaddy.dto.weeklycontent.WeeklyContentSimpleResponse;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.service.WeeklyContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weekly")
@RequiredArgsConstructor
public class WeeklyContent {
    private final WeeklyContentService service;
    private final WeeklyContentService weeklyContentService;

    @GetMapping("/current")
    public ResponseEntity<WeeklyContentResponse> getCurrentWeeklyContent(
            @RequestParam Long babyId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(weeklyContentService.getWeeklyContent(babyId));
    }

    @GetMapping("/{week}")
    public ResponseEntity<WeeklyContentSimpleResponse> getWeeklyContent(
            @PathVariable int week
    ) {
        return ResponseEntity.ok(weeklyContentService.getWeeklyContentSimple(week));
    }
}
