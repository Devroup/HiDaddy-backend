package Devroup.hidaddy.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "baby_comment",  
    uniqueConstraints = @UniqueConstraint(columnNames = {"week_start", "week_end"})
)
public class BabyComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int weekStart;  // 시작 주차

    @Column(nullable = false)
    private int weekEnd;    // 끝 주차

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;
}
