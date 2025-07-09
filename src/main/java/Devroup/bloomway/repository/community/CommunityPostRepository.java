package Devroup.bloomway.repository.community;

import Devroup.bloomway.entity.CommunityPost;
import Devroup.bloomway.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    // 생성일 기준 내림차순으로 게시글 조회
    Page<CommunityPost> findAllByOrderByCreatedAtDesc(Pageable pageable);
    // 게시글 존재 여부 확인
    boolean existsByIdAndUser(Long id, User user);
} 