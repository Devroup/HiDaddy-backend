package Devroup.hidaddy.dto.mission;

import Devroup.hidaddy.entity.Mission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MissionResponse {
    private Long id;
    private String title;
    private String description;
    private String keyword1;
    private String keyword2;
    private String keyword3;
    private Boolean keyword1Success;
    private Boolean keyword2Success;
    private Boolean keyword3Success;
    private String imageUrl; 
    private String content;
    private LocalDateTime createdAt;
} 