package Devroup.hidaddy.dto.community;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;   
import lombok.Builder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityPostRequest {
    private String title;
    private String content;
    private String imageUrl;
} 