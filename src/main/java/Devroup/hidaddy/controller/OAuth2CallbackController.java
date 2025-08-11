package Devroup.hidaddy.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/login/oauth2/code") // /api prefix 적용
@RequiredArgsConstructor
public class OAuth2CallbackController {

    @GetMapping("/kakao")
    @Operation(summary = "카카오 로그인 콜백", description = "카카오 인가 코드를 받아 앱으로 리디렉션합니다.")
    public void redirectKakao(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws IOException {
        redirectToApp("kakao", code, response);
    }

    @GetMapping("/naver")
    @Operation(summary = "네이버 로그인 콜백", description = "네이버 인가 코드를 받아 앱으로 리디렉션합니다.")
    public void redirectNaver(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws IOException {
        redirectToApp("naver", code, response);
    }

    @GetMapping("/google")
    @Operation(summary = "구글 로그인 콜백", description = "구글 인가 코드를 받아 앱으로 리디렉션합니다.")
    public void redirectGoogle(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws IOException {
        redirectToApp("google", code, response);
    }

    private void redirectToApp(String provider, String code, HttpServletResponse response) throws IOException {
        log.info("📥 [{} 콜백] 인가 코드 수신: {}", provider.toUpperCase(), code);
        String redirectUrl = "hidaddy://callback?provider=" + provider + "&code=" + code;
        log.info("🔀 앱으로 리디렉션: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}