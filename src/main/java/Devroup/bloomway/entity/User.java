package Devroup.bloomway.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String partner_phone;
    private String login_type;
    private String social_id;
    private LocalDateTime createAt;

    // 객체 생성시 자동으로 createAt 날짜 지정
    @PrePersist
    protected void onCreate() {
        this.createAt = LocalDateTime.now();
    }

    public User(String name,  String email, String phone, String partner_phone, String login_type, String social_id) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.partner_phone = partner_phone;
        this.login_type = login_type;
        this.social_id = social_id;
    }
}