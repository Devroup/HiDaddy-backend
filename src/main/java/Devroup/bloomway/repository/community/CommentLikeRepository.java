package Devroup.bloomway.repository.community;

import Devroup.bloomway.entity.CommentLike;
import Devroup.bloomway.entity.CommunityComment;
import Devroup.bloomway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    // 댓글과 사용자 조합으로 좋아요 조회
    Optional<CommentLike> findByCommentAndUser(CommunityComment comment, User user);
    // 좋아요 존재 여부 확인
    boolean existsByCommentAndUser(CommunityComment comment, User user);
    // 좋아요 삭제
    void deleteByCommentAndUser(CommunityComment comment, User user);
} 