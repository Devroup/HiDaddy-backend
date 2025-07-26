package Devroup.bloomway.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "selected_baby_id")
    private Long selectedBabyId;

    @Column(nullable = true)
    private String name;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(name = "partner_phone")
    private String partnerPhone;

    @Column(name = "login_type")
    private String loginType;

    @Column(name = "social_id")
    private String socialId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Baby> babies = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<CommunityPost> communityPosts = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<CommunityComment> communityComments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ChatLog> chatLogs = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<EmotionDiary> emotionDiaries = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<MissionLog> missionLogs = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<RewardLog> rewardLogs = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Album> albums = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SentMessage> sentMessages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    public void setId(Long id) {
        this.id = id;
    }

    public void setSelectedBabyId(Long selectedBabyId) {
        this.selectedBabyId = selectedBabyId;
    }
    public User(String name, String email, String phone, String partnerPhone,
                String loginType, String socialId) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.partnerPhone = partnerPhone;
        this.loginType = loginType;
        this.socialId = socialId;
    }
}
