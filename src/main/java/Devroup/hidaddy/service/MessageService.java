package Devroup.hidaddy.service;

import Devroup.hidaddy.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {

    @Value("${infobip.api.key}")
    private String apiKey;

    @Value("${infobip.api.url}")
    private String apiUrl; // 예: https://2m84m6.api.infobip.com/sms/2/text/advanced

    public String sendSmsToPartner(User user, String text) {
        String partnerPhone = user.getPartnerPhone();
        
        if (partnerPhone == null || partnerPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("배우자 번호가 등록되지 않았습니다.");
        }

        RestTemplate restTemplate = new RestTemplate();

        // 요청 JSON 구성
        Map<String, Object> payload = Map.of(
            "messages", Collections.singletonList(
                Map.of(
                    "from", "InfoSMS",
                    "destinations", Collections.singletonList(Map.of("to", partnerPhone)),
                    "text", text
                )
            )
        );

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "App " + apiKey);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        // Infobip API 호출
        ResponseEntity<String> response = restTemplate.exchange(
            apiUrl, HttpMethod.POST, requestEntity, String.class
        );

        return response.getBody();
    }
}
