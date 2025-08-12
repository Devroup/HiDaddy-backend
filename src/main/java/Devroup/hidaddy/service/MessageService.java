package Devroup.hidaddy.service;

import Devroup.hidaddy.dto.message.MessageResponse;

import Devroup.hidaddy.entity.User;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    @Value("${coolsms.api-key}") String apiKey;
    @Value("${coolsms.api-secret}") String apiSecret;
    @Value("${coolsms.domain:https://api.solapi.com}") String domain;
    @Value("${coolsms.from}") String from;

    public MessageResponse sendSmsToPartner(User user, String text) {
        DefaultMessageService messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, domain);

        // 1) to 값에서 하이픈 제거\
        String partnerPhone = user.getPartnerPhone().replaceAll("-", "").trim();

        if (partnerPhone == null || partnerPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("배우자 번호가 등록되지 않았습니다.");
        }

        Message msg = new Message();
        msg.setFrom(from);
        msg.setTo(partnerPhone); // 하이픈 제거된 번호 사용
        msg.setText(text);

        SingleMessageSentResponse res = messageService.sendOne(new SingleMessageSendingRequest(msg));

        return new MessageResponse(
                res.getMessageId(),
                res.getStatusCode(),
                res.getTo()
        );
    }
}
