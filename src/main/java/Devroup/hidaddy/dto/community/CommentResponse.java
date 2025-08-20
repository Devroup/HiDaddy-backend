package Devroup.hidaddy.dto.community;

import Devroup.hidaddy.entity.CommunityComment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private Long authorId;
    private String authorName;
    private String authorProfileImageUrl;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private boolean isLiked;

    public static CommentResponse from(CommunityComment comment, boolean isLiked) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getUser() != null ? comment.getUser().getId() : null)
                .authorName(comment.getUser() != null ? comment.getUser().getName() : "Unknown")
                .authorProfileImageUrl(comment.getUser() != null ? comment.getUser().getProfileImageUrl() : null)
                .likeCount(comment.getLike())
                .createdAt(comment.getCreatedAt())
                .isLiked(isLiked)
                .build();
    }
} 