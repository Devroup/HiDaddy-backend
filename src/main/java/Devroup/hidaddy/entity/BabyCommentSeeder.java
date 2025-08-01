package Devroup.hidaddy.entity;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import Devroup.hidaddy.repository.user.BabyCommentRepository;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BabyCommentSeeder {

    private final BabyCommentRepository babyCommentRepository;

    @PostConstruct
    public void seedBabyComments() {
        if (babyCommentRepository.count() == 0) { // DB 비어있을 때만 실행
            List<BabyComment> comments = List.of(
                BabyComment.builder().weekStart(1).weekEnd(4)
                    .comment("세상에서 가장 작은 기적이 조용히 엄마 뱃속에 찾아와 포근한 둥지를 틀고 있어요.").build(),
                BabyComment.builder().weekStart(5).weekEnd(5)
                    .comment("콩알만 한 아기에게 두근두근 작은 심장이 생기며, 생명의 불빛이 켜졌어요.").build(),
                BabyComment.builder().weekStart(6).weekEnd(6)
                    .comment("조그마한 심장 소리가 엄마에게 속삭이듯 ‘나 여기 있어요’라고 말해요.").build(),
                BabyComment.builder().weekStart(7).weekEnd(7)
                    .comment("조그만 팔다리가 움찔거리며 세상에 인사할 준비를 하고 있대요.").build(),
                BabyComment.builder().weekStart(8).weekEnd(8)
                    .comment("이제 조금씩 사람 모습을 갖춰가며, 세상과 만날 준비를 시작했어요.").build(),
                BabyComment.builder().weekStart(9).weekEnd(9)
                    .comment("작은 얼굴에 눈, 코, 입이 생기며 귀여운 표정을 지을 날이 머지않았어요.").build(),
                BabyComment.builder().weekStart(10).weekEnd(10)
                    .comment("손가락, 발가락 하나 하나가 정성스럽게 만들어지고 있어요.").build(),
                BabyComment.builder().weekStart(11).weekEnd(11)
                    .comment("하품도 하고 꼼지락거리며, 하루하루 생동감이 가득해지고 있어요.").build(),
                BabyComment.builder().weekStart(12).weekEnd(12)
                    .comment("우리 아기는 더욱 건강하게 자라나고 있어요.").build(),
                BabyComment.builder().weekStart(13).weekEnd(16)
                    .comment("엄마의 목소리에 귀 기울이며, 작고 따뜻한 세상을 느끼기 시작했어요.").build(),
                BabyComment.builder().weekStart(17).weekEnd(17)
                    .comment("귀가 생기고, 이제 바깥 세상의 소리에 조금씩 반응할 수 있어요.").build(),
                BabyComment.builder().weekStart(18).weekEnd(18)
                    .comment("작은 발로 톡톡, 엄마에게 ‘여기 있어요’ 하고 인사하는 중이에요.").build(),
                BabyComment.builder().weekStart(19).weekEnd(19)
                    .comment("이제 아기의 감각들이 하나둘 깨어나고 있어요, 놀랍고 신비롭죠.").build(),
                BabyComment.builder().weekStart(20).weekEnd(20)
                    .comment("엄마와 함께한 시간도 벌써 절반, 점점 더 엄마를 닮아가고 있어요.").build(),
                BabyComment.builder().weekStart(21).weekEnd(24)
                    .comment("손으로 얼굴을 만지고, 발차기도 하며 세상과 교감하는 연습을 하고 있어요.").build(),
                BabyComment.builder().weekStart(25).weekEnd(25)
                    .comment("이제는 꿈도 꾼대요, 어떤 세상을 상상하고 있을까요?").build(),
                BabyComment.builder().weekStart(26).weekEnd(26)
                    .comment("아기의 표정에는 감정이 담기기 시작해요, 웃는 모습도 보일 수 있어요.").build(),
                BabyComment.builder().weekStart(27).weekEnd(27)
                    .comment("숨 쉬는 연습을 하며, 세상에 나갈 날을 기다리고 있어요.").build(),
                BabyComment.builder().weekStart(28).weekEnd(32)
                    .comment("자세를 바꿔가며 엄마 뱃속에서 가장 편한 포지션을 찾고 있어요.").build(),
                BabyComment.builder().weekStart(33).weekEnd(33)
                    .comment("하루하루 통통하게 살이 올라 사랑스러움이 가득해지고 있어요.").build(),
                BabyComment.builder().weekStart(34).weekEnd(34)
                    .comment("엄마 심장 소리를 들으며 가장 안전하고 따뜻한 곳에서 쉬고 있어요.").build(),
                BabyComment.builder().weekStart(35).weekEnd(35)
                    .comment("작은 손과 발이 제법 단단해졌고, 어느새 다 커버렸네요.").build(),
                BabyComment.builder().weekStart(36).weekEnd(40)
                    .comment("이제 엄마 품을 떠나 세상을 만나러 갈 준비를 마쳤어요, 곧 만나요!").build()
            );

            babyCommentRepository.saveAll(comments);
        }
    }
}
