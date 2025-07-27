package Devroup.hidaddy.service.community;

import Devroup.hidaddy.dto.community.*;
import Devroup.hidaddy.entity.*;
import Devroup.hidaddy.repository.community.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import Devroup.hidaddy.util.S3Uploader;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {
    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final S3Uploader s3Uploader;
    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;
    
    // 게시글 목록 조회
    public Page<CommunityPostResponse> getPosts(Pageable pageable, User currentUser) {
        // 게시글 조회
        Page<CommunityPost> posts = postRepository.findAll(pageable);
        // 각 게시글에 대해 현재 사용자의 좋아요 여부를 확인하여 매핑 처리
        return posts.map(post -> {
            boolean isLiked = postLikeRepository.existsByPostAndUser(post, currentUser);
            return CommunityPostResponse.from(post, isLiked);
        });
    }

    // 게시글 작성
    @Transactional 
    // 트랜잭션 처리를 보장 -> 모든 작업이 성공적으로 완료되거나 하나라도 실패하면 모든 작업이 롤백됨 -> 데이터 일관성 유지 
    public CommunityPostResponse createPost(String content, MultipartFile image, User user) {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = s3Uploader.upload(image, "community");
            imageUrl = cloudFrontDomain + "/" + imageUrl;
        }

        CommunityPost post = CommunityPost.builder()
            .content(content)
            .imageUrl(imageUrl)
            .user(user)
            .build();
        postRepository.save(post);
        return CommunityPostResponse.from(post, false); // 응답 DTO 생성
    }

    // 게시글 수정
    @Transactional
    public CommunityPostResponse updatePost(Long postId, String content, MultipartFile image, User currentUser) {
        // 게시글 존재 여부 확인
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 인증이 구현되지 않은 상태에서는 작성자 검증을 건너뜀
        if (currentUser != null) {
            // 게시글 작성자와 현재 사용자가 다른 경우
            if (post.getUser() != null && !post.getUser().equals(currentUser)) {
                throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
            }
        }

        // 게시글 내용 수정
        post.setContent(content);

        if (image != null && !image.isEmpty()) {
            // 기존 이미지 삭제
            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                String imageKey = post.getImageUrl().replace(cloudFrontDomain + "/", "");
                s3Uploader.delete(imageKey);
            }
            // 새 이미지 업로드
            String imageUrl = s3Uploader.upload(image, "community");
            imageUrl = cloudFrontDomain + "/" + imageUrl;
            post.setImageUrl(imageUrl);
        }

        // 수정된 게시글 저장 (더티 체킹으로 자동 업데이트)
        CommunityPost updatedPost = postRepository.save(post);
        
        // 현재 사용자의 좋아요 여부 확인
        boolean isLiked = postLikeRepository.existsByPostAndUser(updatedPost, currentUser);
        
        // 수정된 게시글을 DTO로 변환해서 반환
        return CommunityPostResponse.from(updatedPost, isLiked);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, User currentUser) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 인증이 구현되지 않은 상태에서는 작성자 검증을 건너뜀
        if (currentUser != null) {
            // 게시글 작성자와 현재 사용자가 다른 경우
            if (post.getUser() != null && !post.getUser().equals(currentUser)) {
                throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
            }
        }
        // 기존 이미지 삭제
        if(post.getImageUrl() != null && !post.getImageUrl().isEmpty()){
            String imageKey = post.getImageUrl().replace(cloudFrontDomain + "/", "");
            s3Uploader.delete(imageKey);
        }
        // 게시글 삭제
        postRepository.delete(post);
    }

    // 특정 게시글의 댓글 목록 조회
    public Page<CommentResponse> getComments(Long postId, Pageable pageable, User currentUser) {
        // 게시글 존재 여부 확인
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 해당 게시글의 댓글들을 페이징하여 조회
        Page<CommunityComment> comments = commentRepository.findByPostOrderByCreatedAtAsc(post, pageable);
        
        // 각 댓글에 대해 현재 사용자의 좋아요 여부를 확인하여 매핑 처리
        return comments.map(comment -> {
            boolean isLiked = commentLikeRepository.existsByCommentAndUser(comment, currentUser);
            return CommentResponse.from(comment, isLiked);
        });
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
        
        // 게시글의 댓글 수 증가
        post.setCommentCount(post.getCommentCount() + 1);
        
        // 게시글에 댓글 추가
        post.getComments().add(savedComment);
        
        // 저장된 댓글을 DTO로 변환해서 반환
        return CommentResponse.from(savedComment, false);
    }

    // 댓글 수정
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, User currentUser) {
        // 댓글 존재 여부 확인
        CommunityComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 인증이 구현되지 않은 상태에서는 작성자 검증을 건너뜀
        if (currentUser != null) {
            // 댓글 작성자와 현재 사용자가 다른 경우
            if (comment.getUser() != null && !comment.getUser().equals(currentUser)) {
                throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
            }
        }

        // 댓글 내용 수정
        comment.updateContent(request.getContent());

        // 수정된 댓글 저장 (더티 체킹으로 자동 업데이트)
        CommunityComment updatedComment = commentRepository.save(comment);
        
        // 현재 사용자의 좋아요 여부 확인
        boolean isLiked = commentLikeRepository.existsByCommentAndUser(updatedComment, currentUser);
        
        // 수정된 댓글을 DTO로 변환해서 반환
        return CommentResponse.from(updatedComment, isLiked);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, User currentUser) {
        // 댓글 존재 여부 확인
        CommunityComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 인증이 구현되지 않은 상태에서는 작성자 검증을 건너뜀
        if (currentUser != null) {
            // 댓글 작성자와 현재 사용자가 다른 경우
            if (comment.getUser() != null && !comment.getUser().equals(currentUser)) {
                throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
            }
        }

        // 게시글의 댓글 수 감소
        CommunityPost post = comment.getPost();
        post.setCommentCount(post.getCommentCount() - 1);

        // 댓글 삭제
        commentRepository.delete(comment);
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