package Devroup.bloomway.controller;

import Devroup.bloomway.dto.emotiondiary.request.EmotionDiaryCreateRequest;
import Devroup.bloomway.dto.emotiondiary.request.EmotionDiaryUpdateRequest;
import Devroup.bloomway.dto.emotiondiary.response.EmotionDiaryResponse;
import Devroup.bloomway.entity.User;
import Devroup.bloomway.service.EmotionDiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/emotion-diaries")
@RequiredArgsConstructor
public class EmotionDiaryController {
    private final EmotionDiaryService emotionDiaryService;

    // 감정일기 생성 (create)
    @PostMapping
    public ResponseEntity<Void> createEmotionDiary(
            @AuthenticationPrincipal User currentUser,
            @RequestBody EmotionDiaryCreateRequest dto
    ) {
        emotionDiaryService.create(dto, currentUser);
        return ResponseEntity.ok().build();
    }

    // 감정일기 조회 (read)
    // 감정일기 목록 조회
    @GetMapping
    public ResponseEntity<List<EmotionDiaryResponse>> readEmotionDiary(
            // 범위 지정하여 범위 내의 감정일기 조회
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate end,
            @AuthenticationPrincipal User currentUser
    ) {
        List<EmotionDiaryResponse> responses = emotionDiaryService.readEmotionDiary(currentUser, start, end);
        return ResponseEntity.ok(responses);
    }

    // 특정 날짜의 감정일기에 접근
    @GetMapping("/{date}")
    public ResponseEntity<EmotionDiaryResponse> readEmotionDiaryByDate(
            // 특정 날짜로 감정일기에 접근 후 조회
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal User currentUser
    ) {
        EmotionDiaryResponse response = emotionDiaryService.readEmotionDiaryByDate(currentUser, date);
        return ResponseEntity.ok(response);
    }

    // 감정일기 수정 (update)
    @PutMapping("/{date}")
    public ResponseEntity<EmotionDiaryResponse> updateEmotionDiary(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody EmotionDiaryUpdateRequest request
    ) {
        EmotionDiaryResponse response = emotionDiaryService.updateEmotionDiary(currentUser, date, request);
        return ResponseEntity.ok(response);
    }

    // 감정일기 삭제 (delete)
    @DeleteMapping("/{date}")
    public ResponseEntity<Void> deleteEmotionDiary(
            @AuthenticationPrincipal User currentUser,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        emotionDiaryService.deleteEmotionDiary(currentUser, date);
        return ResponseEntity.ok().build();
    }
}
