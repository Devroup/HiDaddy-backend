package Devroup.hidaddy.controller.community;

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
            @RequestParam(defaultValue = "20") int size
            // 현재 로그인한 사용자의 정보를 자동으로 주입
            // @AuthenticationPrincipal User currentUser
    ) {
        // 페이징 처리된 게시글 목록과 각 게시글에 대한 사용자의 좋아요 여부
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        // return ResponseEntity.ok(communityService.getPosts(pageable, currentUser));
        return ResponseEntity.ok(communityService.getPosts(pageable, null));
    }

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    @PostMapping
    public ResponseEntity<CommunityPostResponse> createPost(
            // JSON 형식으로 전달된 데이터를 자동으로 매핑
            @RequestBody CommunityPostRequest request
            // @AuthenticationPrincipal User currentUser    
    ) {
        // return ResponseEntity.ok(communityService.createPost(request, currentUser));
        return ResponseEntity.ok(communityService.createPost(request, null));
    }

    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
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

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
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

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 댓글 목록을 내림차순으로 조회합니다.")
    @GetMapping("/{postId}/comments")
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long postId,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size
            // 현재 로그인한 사용자의 정보를 자동으로 주입
            // @AuthenticationPrincipal User currentUser
    ) {
        // 특정 게시글의 페이징 처리된 댓글 목록과 각 댓글에 대한 사용자의 좋아요 여부
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        // return ResponseEntity.ok(communityService.getComments(postId, pageable, currentUser));
        return ResponseEntity.ok(communityService.getComments(postId, pageable, null));
    }

    @Operation(summary = "댓글 작성", description = "특정 게시글에 댓글을 작성합니다.")
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

    @Operation(summary = "댓글 수정", description = "기존 댓글을 수정합니다.")
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

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId
            // @AuthenticationPrincipal User currentUser
    ) {
        communityService.deleteComment(commentId, null);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시글 좋아요 토글", description = "게시글의 좋아요를 추가하거나 취소합니다.")
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

    @Operation(summary = "댓글 좋아요 토글", description = "댓글의 좋아요를 추가하거나 취소합니다.")
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