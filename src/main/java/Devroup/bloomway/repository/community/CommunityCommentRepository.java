package Devroup.bloomway.repository.community;

import Devroup.bloomway.entity.CommunityComment;
import Devroup.bloomway.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    // 게시글에 대한 댓글 목록 조회
    List<CommunityComment> findByPostOrderByCreatedAtDesc(CommunityPost post);
} 