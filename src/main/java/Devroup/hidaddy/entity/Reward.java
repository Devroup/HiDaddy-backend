package Devroup.hidaddy.entity;

import jakarta.persistence.*;
import lombok.Getter;   
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reward")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    private Integer price;

    @OneToMany(mappedBy = "reward", cascade = CascadeType.ALL)
    private List<RewardLog> rewardLogs = new ArrayList<>();
} 