package Devroup.bloomway.repository.community;

import Devroup.bloomway.entity.CommunityComment;
import Devroup.bloomway.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    // 특정 게시글의 댓글들을 생성일 기준 오름차순으로 페이징하여 조회
    Page<CommunityComment> findByPostOrderByCreatedAtAsc(CommunityPost post, Pageable pageable);
} 