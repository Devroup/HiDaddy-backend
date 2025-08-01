package Devroup.hidaddy.controller;

import Devroup.hidaddy.dto.community.*;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.service.community.CommunityService;   
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@Tag(name = "Community", description = "커뮤니티 게시글 및 댓글 API")
public class CommunityController {
    private final CommunityService communityService;

    @Operation(summary = "게시글 목록 조회", description = "페이징 처리된 게시글 목록을 내림차순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<CommunityPostResponse>> getPosts(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser
    ) {
        // 페이징 처리된 게시글 목록과 각 게시글에 대한 사용자의 좋아요 여부
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(communityService.getPosts(pageable, currentUser));
    }

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    @PostMapping
    public ResponseEntity<CommunityPostResponse> createPost(
            @RequestBody CommunityPostRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(communityService.createPost(request, currentUser));
    }

    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    @PutMapping("/{postId}")
    public ResponseEntity<CommunityPostResponse> updatePost(
            @PathVariable Long postId,
            @RequestBody CommunityPostRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(communityService.updatePost(postId, request, currentUser));
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser
    ) {
        communityService.deletePost(postId, currentUser);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 댓글 목록을 내림차순으로 조회합니다.")
    @GetMapping("/{postId}/comments")
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long postId,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser
    ) {
        // 특정 게시글의 페이징 처리된 댓글 목록과 각 댓글에 대한 사용자의 좋아요 여부
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return ResponseEntity.ok(communityService.getComments(postId, pageable, currentUser));
    }

    @Operation(summary = "댓글 작성", description = "특정 게시글에 댓글을 작성합니다.")
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(communityService.createComment(postId, request, currentUser));
    }

    @Operation(summary = "댓글 수정", description = "기존 댓글을 수정합니다.")
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            // 수정할 댓글 ID
            @PathVariable Long commentId,
            // 수정할 댓글 내용
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(communityService.updateComment(commentId, request, currentUser));
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser
    ) {
        communityService.deleteComment(commentId, currentUser);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시글 좋아요 토글", description = "게시글의 좋아요를 추가하거나 취소합니다.")
    @PostMapping("/{postId}/like")
    // togglePostLike 메서드 -> 이미 좋아요한 경우 취소, 아닌 경우 좋아용
    public ResponseEntity<Void> togglePostLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser
    ) {
        communityService.togglePostLike(postId, currentUser);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 좋아요 토글", description = "댓글의 좋아요를 추가하거나 취소합니다.")
    @PostMapping("/{postId}/comments/{commentId}/like")
    // toggleCommentLike 메서드 -> 이미 좋아요한 경우 취소, 아닌 경우 좋아용
    public ResponseEntity<Void> toggleCommentLike(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser
    ) {
        communityService.toggleCommentLike(commentId, currentUser);
        return ResponseEntity.ok().build();
    }
} 