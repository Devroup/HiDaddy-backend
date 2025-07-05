package Devroup.bloomway.repository.community;

import Devroup.bloomway.entity.PostLike;
import Devroup.bloomway.entity.CommunityPost;
import Devroup.bloomway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    // 게시글과 사용자 조합으로 좋아요 조회
    Optional<PostLike> findByPostAndUser(CommunityPost post, User user);
    // 좋아요 존재 여부 확인
    boolean existsByPostAndUser(CommunityPost post, User user);
    // 좋아요 삭제
    void deleteByPostAndUser(CommunityPost post, User user);
} 