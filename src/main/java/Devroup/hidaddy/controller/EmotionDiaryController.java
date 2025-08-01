package Devroup.hidaddy.controller;

import Devroup.hidaddy.dto.emotionDiary.*;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.security.UserDetailsImpl;
import Devroup.hidaddy.service.EmotionDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.cglib.core.Local;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/emotion-diaries")
@RequiredArgsConstructor
@Tag(name = "Emotion-Diary", description = "감정일기 API")
public class EmotionDiaryController {
    private final EmotionDiaryService emotionDiaryService;

    // 감정일기 생성 (create)
    @Operation(summary = "감정일기 생성", description = "특정 날짜의 감정일기를 생성합니다.")
    @PostMapping
    public ResponseEntity<Void> createEmotionDiary(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody EmotionDiaryCreateRequest dto
    ) {
        User currentUser = userDetails.getUser();
        emotionDiaryService.create(dto, currentUser);
        return ResponseEntity.ok().build();
    }

    // 감정일기 조회 (read)
    // 감정일기 목록 조회
    @Operation(summary = "감정일기 목록 조회", description = "캘린더 표시를 위해 선택한 날짜 범위의 감정일기를 날짜 오름차순으로 조회합니다.")
    @GetMapping
    public List<EmotionDiaryMonthResponse> readEmotionDiary(
            // 범위 지정하여 범위 내의 감정일기 조회
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        User currentUser = userDetails.getUser();
        return emotionDiaryService.readEmotionDiary(currentUser, startDate, endDate);
    }

    // 특정 날짜의 감정일기에 접근
    @Operation(summary = "개별 감정일기 조회", description = "특정 날짜의 감정일기를 조회합니다.")
    @GetMapping("/{date}")
    public ResponseEntity<EmotionDiaryResponse> readEmotionDiaryByDate(
            // 특정 날짜로 감정일기에 접근 후 조회
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        User currentUser = userDetails.getUser();
        EmotionDiaryResponse response = emotionDiaryService.readEmotionDiaryByDate(currentUser, date);
        return ResponseEntity.ok(response);
    }

    // 감정일기 수정 (update)
    @Operation(summary = "특정 감정일기 수정", description = "해당하는 날짜의 감정일기를 수정합니다.")
    @PutMapping("/{date}")
    public ResponseEntity<EmotionDiaryResponse> updateEmotionDiary(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody EmotionDiaryUpdateRequest request
    ) {
        User currentUser = userDetails.getUser();
        EmotionDiaryResponse response = emotionDiaryService.updateEmotionDiary(currentUser, date, request);
        return ResponseEntity.ok(response);
    }

    // 감정일기 삭제 (delete)
    @Operation(summary = "특정 감정일기 삭제", description = "해당하는 날짜의 감정일기를 삭제합니다.")
    @DeleteMapping("/{date}")
    public ResponseEntity<Void> deleteEmotionDiary(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        User currentUser = userDetails.getUser();
        emotionDiaryService.deleteEmotionDiary(currentUser, date);
        return ResponseEntity.ok().build();
    }
}
