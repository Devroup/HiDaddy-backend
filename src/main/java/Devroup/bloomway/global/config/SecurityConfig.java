package Devroup.bloomway.global.config;

import Devroup.bloomway.security.OAuth2AuthenticationSuccessHandler;
import Devroup.bloomway.jwt.JwtAuthenticationFilter;
import Devroup.bloomway.jwt.JwtUtil;
import Devroup.bloomway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login", "/oauth2/**", "/auth/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl("/api/login/login-success", true)
                        .successHandler(oAuth2AuthenticationSuccessHandler)// 무조건 "/login-success"로 이동하게 설정
                )
                .logout(logout -> logout
                        .logoutUrl("/api/login/logout")             // POST /api/logout 경로
                        .logoutSuccessUrl("/api/login")             // 로그아웃 후 리디렉션
                        .invalidateHttpSession(true)
                        .deleteCookies("accessToken", "refreshToken")
                        .permitAll()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userRepository),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

