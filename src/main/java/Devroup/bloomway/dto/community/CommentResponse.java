package Devroup.bloomway.dto.community;

import Devroup.bloomway.entity.CommunityComment;
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
    private String authorName;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private boolean isLiked;

    public static CommentResponse from(CommunityComment comment, boolean isLiked) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorName(comment.getUser() != null ? comment.getUser().getName() : "Unknown")
                .likeCount(comment.getLike())
                .createdAt(comment.getCreatedAt())
                .isLiked(isLiked)
                .build();
    }
} 