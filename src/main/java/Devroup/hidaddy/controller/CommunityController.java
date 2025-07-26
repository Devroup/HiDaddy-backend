package Devroup.hidaddy.controller.community;

import Devroup.hidaddy.dto.community.*;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.service.community.CommunityService;   
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            // 한 페이지당 20개의 게시글을 가져오도록 기본값 설정, 생성일 기준 내림차순 정렬
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
            // 현재 로그인한 사용자의 정보를 자동으로 주입
            // @AuthenticationPrincipal User currentUser
    ) {
        // 페이징 처리된 게시글 목록과 각 게시글에 대한 사용자의 좋아요 여부
        // return ResponseEntity.ok(communityService.getPosts(pageable, currentUser));
        return ResponseEntity.ok(communityService.getPosts(pageable, null));
    }

    @PostMapping
    public ResponseEntity<CommunityPostResponse> createPost(
            // JSON 형식으로 전달된 데이터를 자동으로 매핑
            @RequestBody CommunityPostRequest request
            // @AuthenticationPrincipal User currentUser    
    ) {
        // return ResponseEntity.ok(communityService.createPost(request, currentUser));
        return ResponseEntity.ok(communityService.createPost(request, null));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<CommunityPostResponse> updatePost(
            // 수정할 게시글 ID
            @PathVariable Long postId,
            // 수정할 내용이 담긴 요청 데이터
            @RequestBody CommunityPostRequest request
            // 작성자 본인만 수정 가능하도록 서비스 계층에서 검증 예정
            // @AuthenticationPrincipal User currentUser
    ) {
        // return ResponseEntity.ok(communityService.updatePost(postId, request, currentUser));
        return ResponseEntity.ok(communityService.updatePost(postId, request, null));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            // 삭제할 게시글 ID
            @PathVariable Long postId
            // 작성자 본인만 삭제 가능하도록 서비스 계층에서 검증 예정
            // @AuthenticationPrincipal User currentUser
    ) {
        // communityService.deletePost(postId, currentUser);
        communityService.deletePost(postId, null);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long postId,
            // 한 페이지당 10개의 댓글을 가져오도록 기본값 설정, 생성일 기준 오름차순 정렬
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
            // 현재 로그인한 사용자의 정보를 자동으로 주입
            // @AuthenticationPrincipal User currentUser
    ) {
        // 특정 게시글의 페이징 처리된 댓글 목록과 각 댓글에 대한 사용자의 좋아요 여부
        // return ResponseEntity.ok(communityService.getComments(postId, pageable, currentUser));
        return ResponseEntity.ok(communityService.getComments(postId, pageable, null));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest request
            // 댓글 작성자는 현재 로그인한 사용자로 자동 지정
            // @AuthenticationPrincipal User currentUser
    ) {
        // return ResponseEntity.ok(communityService.createComment(postId, request, currentUser));
        return ResponseEntity.ok(communityService.createComment(postId, request, null));
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            // 수정할 댓글 ID
            @PathVariable Long commentId,
            // 수정할 댓글 내용
            @RequestBody CommentRequest request
            // 댓글 작성자 본인만 수정 가능하도록 서비스 계층에서 검증 예정
            // @AuthenticationPrincipal User currentUser
    ) {
        // return ResponseEntity.ok(communityService.updateComment(commentId, request, currentUser));
        return ResponseEntity.ok(communityService.updateComment(commentId, request, null));
    }


    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId
            // @AuthenticationPrincipal User currentUser
    ) {
        communityService.deleteComment(commentId, null);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/like")
    // togglePostLike 메서드 -> 이미 좋아요한 경우 취소, 아닌 경우 좋아용
    public ResponseEntity<Void> togglePostLike(
            @PathVariable Long postId
            // @AuthenticationPrincipal User currentUser
    ) {
        // communityService.togglePostLike(postId, currentUser);
        communityService.togglePostLike(postId, null);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comments/{commentId}/like")
    // toggleCommentLike 메서드 -> 이미 좋아요한 경우 취소, 아닌 경우 좋아용
    public ResponseEntity<Void> toggleCommentLike(
            @PathVariable Long commentId
            // @AuthenticationPrincipal User currentUser
    ) {
        // communityService.toggleCommentLike(commentId, currentUser);
        communityService.toggleCommentLike(commentId, null);
        return ResponseEntity.ok().build();
    }
} 