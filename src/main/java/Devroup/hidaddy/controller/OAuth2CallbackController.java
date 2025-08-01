package Devroup.hidaddy.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/login/oauth2/code")
@RequiredArgsConstructor
public class OAuth2CallbackController {

    @GetMapping("/kakao")
    public void redirectKakao(
            @Parameter(description = "카카오 인가 코드", required = true)
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws IOException {
        redirectToApp("kakao", code, response);
    }

    @GetMapping("/naver")
    public void redirectNaver(
            @Parameter(description = "네이버 인가 코드", required = true)
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws IOException {
        redirectToApp("naver", code, response);
    }

    @GetMapping("/google")
    public void redirectGoogle(
            @Parameter(description = "구글 인가 코드", required = true)
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
