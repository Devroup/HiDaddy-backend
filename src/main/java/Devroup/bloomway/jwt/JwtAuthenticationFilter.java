package Devroup.bloomway.jwt;

import Devroup.bloomway.entity.User;
import Devroup.bloomway.repository.UserRepository;
import Devroup.bloomway.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 공개 경로면 인증 없이 필터 통과
        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        // 1. Authorization 헤더에서 토큰 추출
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 2. 토큰 존재 시 검증 및 유저 인증 처리
        if (token != null && jwtUtil.validateToken(token)) {
            Claims claims = jwtUtil.getClaims(token);
            String userIdStr = claims.getSubject();
            Long userId = Long.parseLong(userIdStr);

            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                UserDetailsImpl userDetails = new UserDetailsImpl(user);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 3. 다음 필터로 넘김
        filterChain.doFilter(request, response);
    }


    private boolean isPublicPath(String path) {
        return path.equals("/") ||
                path.startsWith("/api/login") ||
                path.startsWith("/auth") ||
                path.startsWith("/oauth2") ||
                path.startsWith("/css") ||
                path.startsWith("/js") ||
                path.startsWith("/h2-console") ||
                path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs"); // Swagger용
    }
}
