package Devroup.hidaddy.dto.community;

import Devroup.hidaddy.entity.CommunityPost;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityPostDetailResponse {
    private Long id;
    private String content;
    private String imageUrl;
    private Integer likeCount;
    private String authorName;
    private String authorProfileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isLiked;
    private List<CommentResponse> comments;

    public static CommunityPostDetailResponse from(CommunityPost post, boolean isLiked, List<CommentResponse> comments) {
        return CommunityPostDetailResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .likeCount(post.getLikeCount())
                .authorName(post.getUser() != null ? post.getUser().getName() : "Unknown")
                .authorProfileImageUrl(post.getUser() != null ? post.getUser().getProfileImageUrl() : null)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isLiked(isLiked)
                .comments(comments)
                .build();
    }
}
