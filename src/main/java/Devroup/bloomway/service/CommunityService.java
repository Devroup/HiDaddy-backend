package Devroup.bloomway.service.community;

import Devroup.bloomway.dto.community.*;
import Devroup.bloomway.entity.*;
import Devroup.bloomway.repository.community.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {
    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;

    // 게시글 목록 조회
    public Page<CommunityPostResponse> getPosts(Pageable pageable, User currentUser) {
        // 생성일 기준 내림차순으로 게시글 조회
        // 페이징 처리 : Pageable 객체를 통해 페이지 번호, 크기, 정렬 정보를 받음
        Page<CommunityPost> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        // 각 게시글에 대해 현재 사용자의 좋아요 여부를 확인하여 매핑 처리
        return posts.map(post -> {
            boolean isLiked = postLikeRepository.existsByPostAndUser(post, currentUser);
            return CommunityPostResponse.from(post, isLiked);
        });
    }

    // 게시글 작성
    @Transactional 
    // 트랜잭션 처리를 보장 -> 모든 작업이 성공적으로 완료되거나 하나라도 실패하면 모든 작업이 롤백됨 -> 데이터 일관성 유지 
    public CommunityPostResponse createPost(CommunityPostRequest request, User currentUser) {
        // 게시글 엔티티 생성
        CommunityPost post = CommunityPost.builder()
                .user(currentUser)
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .build();

        // 데이터베이스에 저장
        CommunityPost savedPost = postRepository.save(post);
        // 저장된 게시글을 DTO로 변환해서 반환
        // 여기서 false는 해당 게시글에 대한 좋아요 여부 -> 새로 생성된 게시글은 false로 초기화
        return CommunityPostResponse.from(savedPost, false);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, User currentUser) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getUser().equals(currentUser)) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }

    // 댓글 작성
    @Transactional
    public CommentResponse createComment(Long postId, CommentRequest request, User currentUser) {
        // 게시글 존재 여부 확인
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 댓글 엔티티 생성
        CommunityComment comment = CommunityComment.builder()
                .user(currentUser)
                .post(post)
                .content(request.getContent())
                .build();

        // 데이터베이스에 저장
        CommunityComment savedComment = commentRepository.save(comment);
        // 게시글에 댓글 추가
        post.getComments().add(savedComment);
        // 저장된 댓글을 DTO로 변환해서 반환
        // 여기서 false는 해당 댓글에 대한 좋아요 여부 -> 새로 생성된 댓글은 false로 초기화
        return CommentResponse.from(savedComment, false);
    }

    // 게시글 좋아요
    @Transactional
    public void togglePostLike(Long postId, User currentUser) {
        // 게시글 존재 여부 확인
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 이미 좋아요한 경우 취소
        if (postLikeRepository.existsByPostAndUser(post, currentUser)) {
            postLikeRepository.deleteByPostAndUser(post, currentUser);
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            // 좋아요 추가
            PostLike postLike = PostLike.builder()
                    .post(post)
                    .user(currentUser)
                    .build();
            postLikeRepository.save(postLike);
            post.setLikeCount(post.getLikeCount() + 1);
        }
    }

    // 댓글 좋아요
    @Transactional
    public void toggleCommentLike(Long commentId, User currentUser) {
        // 댓글 존재 여부 확인
        CommunityComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 이미 좋아요한 경우 취소
        if (commentLikeRepository.existsByCommentAndUser(comment, currentUser)) {
            commentLikeRepository.deleteByCommentAndUser(comment, currentUser);
            comment.setLike(comment.getLike() - 1);
        } else {
            // 좋아요 추가
            CommentLike commentLike = CommentLike.builder()
                    .comment(comment)
                    .user(currentUser)
                    .build();
            commentLikeRepository.save(commentLike);
            comment.setLike(comment.getLike() + 1);
        }
    }
} 