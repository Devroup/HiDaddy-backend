package Devroup.hidaddy.entity;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import Devroup.hidaddy.repository.weeklycontent.WeeklyContentRepository;

@Component
@RequiredArgsConstructor
public class WeeklyContentSeeder {

    private final WeeklyContentRepository weeklyContentRepository;

    @PostConstruct
    public void seedWeeklyContent() {
        if (weeklyContentRepository.count() == 0) { // 중복 삽입 방지

            weeklyContentRepository.save(
                WeeklyContent.builder()
                    .week(1)
                    .babyContent("우리 아기는 지금, 단 하나의 세포로 시작되는 기적을 겪고 있어요.\n"
                               + "정자와 난자가 만나면서 생명이 잉태되고, 이 조그마한 수정란은 머리카락보다도 작은 0.2mm 정도랍니다.\n"
                               + "이 작은 생명이 지금 이 순간에도 쉼 없이 세포 분열을 하며 자라고 있어요.\n"
                               + "눈에 보이지 않지만, 우리 아기의 첫 걸음이 조용히 시작됐다는 걸 기억해주세요.")
                    .momContent("아직은 엄마가 특별한 변화를 느끼지 못할 수도 있어요.\n"
                               + "겉으론 평소와 비슷해 보이지만, 몸속에서는 새로운 생명을 맞이할 준비가 조금씩 진행되고 있어요.\n"
                               + "혹시 모를 임신에 대비해, 아빠도 엄마의 몸 상태나 감정 변화에 더 민감하게 귀 기울여 주세요.\n"
                               + "작은 관심 하나가 엄마에게 큰 힘이 될 수 있어요.")
                    .healthContent("이 시기는 아직 병원에서 특별한 검사를 받을 필요는 없어요.\n"
                                  + "하지만 아빠로서도 미리 준비할 수 있는 것들이 있어요.\n"
                                  + "라면, 인스턴트 음식, 술, 담배 같은 것들을 줄이는 식습관을 엄마와 함께 실천해보세요.\n"
                                  + "임신을 준비하는 지금, 아빠의 생활 습관도 아기에게 중요한 영향을 줄 수 있답니다.")
                    .build()
            );

            weeklyContentRepository.save(
                WeeklyContent.builder()
                    .week(2)
                    .babyContent("이제 막 생명을 틔운 아기는, 아직 '태아(胎兒)'가 아닌 '태아(胎芽)'라고 불려요.\n"
                               + "작은 수정란은 나팔관을 따라 이동하며 빠르게 세포 분열을 반복하고 있어요.\n"
                               + "겉으론 아무것도 보이지 않지만, 이 작은 세포는 뿌리처럼 자리 잡을 준비를 하고 있답니다.\n"
                               + "우리 아기의 존재는 여전히 조용하지만, 분명히 자라고 있다는 걸 기억해주세요.")
                    .momContent("이 시기에도 엄마의 몸은 겉으론 큰 변화가 없어 보일 수 있어요.\n"
                               + "하지만 몸속에선 임신을 유지하기 위한 호르몬이 활발히 분비되고 있어요.\n"
                               + "예민한 경우에는 감기처럼 으슬으슬하거나 나른한 느낌이 들기도 해요.\n"
                               + "아빠가 살짝 더 신경 써주면, 엄마는 그 마음만으로도 큰 힘을 얻어요.")
                    .healthContent("아직 병원에 갈 단계는 아니지만, 집에서 임신진단 시약으로 확인해볼 수 있어요.\n"
                                  + "첫 소변으로 검사하면 더 정확하답니다. 단, 시기가 너무 이르면 결과가 정확하지 않을 수도 있어요.\n"
                                  + "지금 아빠가 해줄 수 있는 건, 함께 기다려주고 조심스럽게 일상에 변화 주기 시작하는 거예요.\n"
                                  + "특히, 감기 같은 병에 걸리지 않도록 가족 모두 건강에 유의해 주세요. 아빠의 건강도 중요해요!")
                    .build()
            );
        }
    }
}