package Devroup.bloomway.global.api;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

// API 응답 상세 정보
@Getter
@Builder
public class ReasonDto implements BaseCode {

    private HttpStatus httpStatus; // HTTP 상태 코드
    private String code; // 응답 코드
    private String message; // 응답 메시지

    @Override
    public ReasonDto getReason() {
        return this;
    }
}
