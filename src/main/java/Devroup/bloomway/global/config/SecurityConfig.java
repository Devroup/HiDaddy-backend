package Devroup.bloomway.global.config;

import Devroup.bloomway.security.OAuth2AuthenticationSuccessHandler;
import Devroup.bloomway.jwt.JwtAuthenticationFilter;
import Devroup.bloomway.jwt.JwtUtil;
import Devroup.bloomway.repository.user.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // API 인증 정책
                .csrf(AbstractHttpConfigurer::disable)  // CSRF 보호 비활성화
                .cors(AbstractHttpConfigurer::disable)  // CORS 비활성화
                .formLogin(AbstractHttpConfigurer::disable)  // 폼 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)  // HTTP Basic 인증 비활성화
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // 모든 요청 허용
                )

                // 비인증 요청 시 401 JSON 반환
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\": \"인증이 필요합니다.\"}");
                        })
                )

                // 소셜 로그인 성공 시 딥링크 리디렉션 핸들러 사용
                .oauth2Login(oauth -> oauth
                                .successHandler(oAuth2AuthenticationSuccessHandler)
                        // .defaultSuccessUrl("/api/login/login-success", true) // 웹 테스트용 제거
                )

                // JWT 필터 등록
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtUtil, userRepository),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}