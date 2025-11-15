package Devroup.hidaddy.repository.community;

import Devroup.hidaddy.entity.Report;
import Devroup.hidaddy.entity.Report.ReportType;
import Devroup.hidaddy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // 중복 신고 방지: 같은 사용자가 같은 대상을 이미 신고했는지 확인
    boolean existsByReporterAndReportTypeAndTargetId(User reporter, ReportType reportType, Long targetId);

    // 특정 대상(게시글/댓글)에 대한 신고 횟수 조회
    long countByReportTypeAndTargetId(ReportType reportType, Long targetId);
}
