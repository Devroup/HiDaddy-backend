package Devroup.hidaddy.repository.community;

import Devroup.hidaddy.entity.CommunityComment;
import Devroup.hidaddy.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    // 특정 게시글의 댓글들을 생성일 기준 오름차순으로 페이징하여 조회
    Page<CommunityComment> findByPostOrderByCreatedAtAsc(CommunityPost post, Pageable pageable);
    
    // 특정 게시글의 모든 댓글을 생성일 기준 오름차순으로 조회 (페이징 없음)
    List<CommunityComment> findByPostOrderByCreatedAtAsc(CommunityPost post);
} 