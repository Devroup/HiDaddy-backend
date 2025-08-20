package Devroup.hidaddy.dto.community;

import Devroup.hidaddy.entity.CommunityPost;
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
    private String content;
    private String imageUrl;
    private Integer likeCount;
    private Integer commentCount;
    private String authorName;
    private String authorProfileImageUrl;
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isLiked;

    public static CommunityPostResponse from(CommunityPost post, boolean isLiked) {
        return CommunityPostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .authorId(post.getUser() != null ? post.getUser().getId() : null)
                .authorName(post.getUser() != null ? post.getUser().getName() : "Unknown")
                .authorProfileImageUrl(post.getUser() != null ? post.getUser().getProfileImageUrl() : null)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isLiked(isLiked)
                .build();
    }
} 