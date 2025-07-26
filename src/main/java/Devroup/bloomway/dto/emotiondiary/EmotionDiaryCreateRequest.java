package Devroup.bloomway.dto.emotiondiary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmotionDiaryCreateRequest {

    private String content;

    @NotNull
    private LocalDateTime date;

    private String imageUrl;
}
