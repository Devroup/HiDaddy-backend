package Devroup.bloomway.controller;

import Devroup.bloomway.dto.request.EmotionDiaryCreateRequest;
import Devroup.bloomway.entity.EmotionDiary;
import Devroup.bloomway.global.api.ApiResponse;
import Devroup.bloomway.global.api.SuccessCode;
import Devroup.bloomway.service.EmotionDiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@XSlf4j
@RestController
@RequestMapping("/emotion-diaries")
@RequiredArgsConstructor
public class EmotionDiaryController {
    private final EmotionDiaryService emotionDiaryService;

    @PostMapping
    public ApiResponse<?> createEmotionDiary(
            @RequestBody EmotionDiaryCreateRequest request
    ) {
        emotionDiaryService.create(request);
        return ApiResponse.onSuccess(SuccessCode.EMOTIONDIARY_CREATE_SUCCESS, null);
    }
}
