package Devroup.bloomway.jwt;

import Devroup.bloomway.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final String secretKey = "hidaddy-super-secure-jwt-secret-key-2025";
    private final long accessExpirationMs = 1000L * 60 * 60 * 24;   // 24시간
    private final long refreshExpirationMs = 1000L * 60 * 60 * 24 * 365; // 1년

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Access Token 생성
    public String createAccessToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("id", user.getId())
                .claim("socialId", user.getSocialId())
                .claim("loginType", user.getLoginType())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken() {
        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 클레임 추출
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

     // id_token(Base64 인코딩된 JWT)에서 특정 claim 추출
    public static String decodeClaim(String jwt, String claimName) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return null;

            // JWT의 Payload(Base64 URL Safe) 디코딩
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

            // JSON 파싱해서 claim 추출
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(payloadJson);
            return node.has(claimName) ? node.get(claimName).asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static void printDecodedToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                System.out.println("❌ Invalid JWT format");
                return;
            }

            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            System.out.println("🔍 [ID TOKEN PAYLOAD] " + payloadJson);
        } catch (Exception e) {
            System.out.println("❌ Failed to decode id_token: " + e.getMessage());
        }
    }
}
