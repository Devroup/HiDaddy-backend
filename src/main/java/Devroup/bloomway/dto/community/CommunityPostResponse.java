package Devroup.bloomway.dto.community;

import Devroup.bloomway.entity.CommunityPost;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityPostResponse {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private Integer likeCount;
    private Integer commentCount;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isLiked;

    public static CommunityPostResponse from(CommunityPost post, boolean isLiked) {
        return CommunityPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .authorName(post.getUser() != null ? post.getUser().getName() : "Unknown")
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isLiked(isLiked)
                .build();
    }
} 