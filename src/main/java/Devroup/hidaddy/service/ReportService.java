package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.community.ReportResponse;
import Devroup.hidaddy.entity.CommunityComment;
import Devroup.hidaddy.entity.CommunityPost;
import Devroup.hidaddy.entity.Report;
import Devroup.hidaddy.entity.Report.ReportType;
import Devroup.hidaddy.entity.User;
import Devroup.hidaddy.global.exeption.BadRequestException;
import Devroup.hidaddy.repository.community.CommunityCommentRepository;
import Devroup.hidaddy.repository.community.CommunityPostRepository;
import Devroup.hidaddy.repository.community.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;

    @Transactional
    public ReportResponse reportPost(Long postId, User reporter) {
        // 게시글 존재 여부 확인
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BadRequestException("게시글을 찾을 수 없습니다."));

        // 자기 자신의 게시글은 신고할 수 없음
        if (post.getUser().getId().equals(reporter.getId())) {
            throw new BadRequestException("자신의 게시글은 신고할 수 없습니다.");
        }

        // 중복 신고 방지
        if (reportRepository.existsByReporterAndReportTypeAndTargetId(reporter, ReportType.POST, postId)) {
            throw new BadRequestException("이미 신고한 게시글입니다.");
        }

        // 신고 저장
        Report report = Report.builder()
                .reporter(reporter)
                .reportType(ReportType.POST)
                .targetId(postId)
                .build();

        Report savedReport = reportRepository.save(report);

        // 신고 횟수 조회
        long totalReportCount = reportRepository.countByReportTypeAndTargetId(ReportType.POST, postId);

        // TODO: 임계값 초과 시 알림 (예: 3회 이상)
        if (totalReportCount >= 3) {
            log.warn("게시글 ID {}에 대한 신고가 {}회 발생했습니다.", postId, totalReportCount);
            // 이메일 또는 슬랙 알림 전송 로직 추가
        }

        return ReportResponse.from(savedReport, totalReportCount);
    }

    @Transactional
    public ReportResponse reportComment(Long postId, Long commentId, User reporter) {
        // 댓글 존재 여부 확인
        CommunityComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BadRequestException("댓글을 찾을 수 없습니다."));

        // 댓글이 해당 게시글에 속하는지 확인
        if (!comment.getPost().getId().equals(postId)) {
            throw new BadRequestException("해당 게시글의 댓글이 아닙니다.");
        }

        // 자기 자신의 댓글은 신고할 수 없음
        if (comment.getUser().getId().equals(reporter.getId())) {
            throw new BadRequestException("자신의 댓글은 신고할 수 없습니다.");
        }

        // 중복 신고 방지
        if (reportRepository.existsByReporterAndReportTypeAndTargetId(reporter, ReportType.COMMENT, commentId)) {
            throw new BadRequestException("이미 신고한 댓글입니다.");
        }

        // 신고 저장
        Report report = Report.builder()
                .reporter(reporter)
                .reportType(ReportType.COMMENT)
                .targetId(commentId)
                .build();

        Report savedReport = reportRepository.save(report);

        // 신고 횟수 조회
        long totalReportCount = reportRepository.countByReportTypeAndTargetId(ReportType.COMMENT, commentId);

        // TODO: 임계값 초과 시 알림 (예: 3회 이상)
        if (totalReportCount >= 3) {
            log.warn("댓글 ID {}에 대한 신고가 {}회 발생했습니다.", commentId, totalReportCount);
            // 이메일 또는 슬랙 알림 전송 로직 추가
        }

        return ReportResponse.from(savedReport, totalReportCount);
    }
}
