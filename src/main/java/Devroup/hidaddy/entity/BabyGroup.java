package Devroup.hidaddy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class BabyGroup {
    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "babyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Baby> babies = new ArrayList<>();
}
