package Devroup.hidaddy.dto.emotionDiary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmotionDiaryCreateRequest {

    private String content;

    @NotNull
    private LocalDate date;
}
