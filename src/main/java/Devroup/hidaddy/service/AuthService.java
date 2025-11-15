package Devroup.hidaddy.service;

import com.fasterxml.jackson.databind.JsonNode;
import Devroup.hidaddy.global.exeption.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String naverRedirectUri;

    // Authorization Code → id_token, access_token 교환
    public Map<String, String> exchangeCodeForToken(String provider, String code) {
        String tokenUrl;
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        params.add("grant_type", "authorization_code");
        params.add("code", code);

        switch (provider.toLowerCase()) {
            case "google" -> {
                tokenUrl = "https://oauth2.googleapis.com/token";
                params.add("client_id", googleClientId);
                params.add("client_secret", googleClientSecret);
                params.add("redirect_uri", googleRedirectUri);
            }
            case "kakao" -> {
                tokenUrl = "https://kauth.kakao.com/oauth/token";
                params.add("client_id", kakaoClientId);
                params.add("redirect_uri", kakaoRedirectUri);
            }
            case "naver" -> {
                tokenUrl = "https://nid.naver.com/oauth2/token";
                params.add("client_id", naverClientId);
                params.add("client_secret", naverClientSecret);
                params.add("redirect_uri", naverRedirectUri);
                params.add("state", "random");
            }
            default -> throw new BadRequestException("Unsupported provider: " + provider);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        JsonNode response = restTemplate.postForObject(tokenUrl, request, JsonNode.class);
        if (response != null) {
            log.info("🔍 [{}] Token API Response: {}", provider.toUpperCase(), response.toPrettyString());
        } else {
            log.error("❌ [{}] Token API returned null response", provider.toUpperCase());
        }
        if (response == null) {
            throw new RuntimeException("Token exchange failed for " + provider);
        }

        Map<String, String> tokens = new HashMap<>();
        if (response.has("id_token")) tokens.put("id_token", response.get("id_token").asText());
        if (response.has("access_token")) tokens.put("access_token", response.get("access_token").asText());
        return tokens;
    }

    // 네이버 전용: access_token으로 이메일 가져오기
    public String getNaverEmail(String accessToken) {
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        JsonNode response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, JsonNode.class).getBody();

        log.info("🔍 [NAVER USER INFO] {}", response != null ? response.toPrettyString() : "null");

        if (response != null && response.has("response")) {
            JsonNode res = response.get("response");
            return res.has("email") ? res.get("email").asText() : null;
        }
        return null;
    }
}
