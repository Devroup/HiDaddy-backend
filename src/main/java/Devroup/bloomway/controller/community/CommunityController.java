package Devroup.bloomway.controller.community;

import Devroup.bloomway.dto.community.*;
import Devroup.bloomway.entity.User;
import Devroup.bloomway.service.community.CommunityService;   
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    @GetMapping
    public ResponseEntity<Page<CommunityPostResponse>> getPosts(
            // 한 페이지당 20개의 게시글을 가져오도록 기본값 설정
            @PageableDefault(size = 20) Pageable pageable,
            // 현재 로그인한 사용자의 정보를 자동으로 주입
            @AuthenticationPrincipal User currentUser
    ) {
        // 페이징 처리된 게시글 목록과 각 게시글에 대한 사용자의 좋아요 여부
        return ResponseEntity.ok(communityService.getPosts(pageable, currentUser));
    }

    @PostMapping
    public ResponseEntity<CommunityPostResponse> createPost(
            // JSON 형식으로 전달된 데이터를 자동으로 매핑
            @RequestBody CommunityPostRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(communityService.createPost(request, currentUser));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            // 삭제할 게시글 ID
            @PathVariable Long postId,
            // 작성자 본인만 삭제 가능하도록 서비스 계층에서 검증 예정
            @AuthenticationPrincipal User currentUser
    ) {
        communityService.deletePost(postId, currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest request,
            // 댓글 작성자는 현재 로그인한 사용자로 자동 지정
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(communityService.createComment(postId, request, currentUser));
    }

    @PostMapping("/{postId}/like")
    // togglePostLike 메서드 -> 이미 좋아요한 경우 취소, 아닌 경우 좋아용
    public ResponseEntity<Void> togglePostLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser
    ) {
        communityService.togglePostLike(postId, currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comments/{commentId}/like")
    // toggleCommentLike 메서드 -> 이미 좋아요한 경우 취소, 아닌 경우 좋아용
    public ResponseEntity<Void> toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser
    ) {
        communityService.toggleCommentLike(commentId, currentUser);
        return ResponseEntity.ok().build();
    }
} 