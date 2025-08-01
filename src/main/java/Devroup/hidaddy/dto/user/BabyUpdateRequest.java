package Devroup.hidaddy.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BabyUpdateRequest {
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}
