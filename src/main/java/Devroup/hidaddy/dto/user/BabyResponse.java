package Devroup.hidaddy.dto.user;

import Devroup.hidaddy.entity.Baby;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class BabyResponse {
    private Long id;
    private String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") // 날짜 포맷 지정
    private LocalDate dueDate;

    public BabyResponse(Baby baby) {
        this.id = baby.getId();
        this.name = baby.getName();
        this.dueDate = baby.getDueDate().toLocalDate();
    }
}
