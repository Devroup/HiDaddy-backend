package Devroup.hidaddy.dto.community;

import Devroup.hidaddy.entity.Report;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
    private Long reportId;
    private String message;
    private Long totalReportCount;

    public static ReportResponse from(Report report, Long totalReportCount) {
        return ReportResponse.builder()
                .reportId(report.getId())
                .message("신고가 접수되었습니다.")
                .totalReportCount(totalReportCount)
                .build();
    }
}
