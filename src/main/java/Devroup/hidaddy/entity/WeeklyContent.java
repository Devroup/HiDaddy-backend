package Devroup.hidaddy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "weekly_content")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer week;

    private String title;

    @Lob
    private String content;
} 