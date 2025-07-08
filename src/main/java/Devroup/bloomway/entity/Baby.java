package Devroup.bloomway.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity // JPA가 이 클래스를 엔티티로 인식하도록 지정하는 어노테이션
@Table(name = "baby") // 데이터베이스에서 사용될 테이블 이름을 'baby'로 지정
@Getter // Lombok: 모든 필드에 대한 getter 메소드를 자동 생성
@NoArgsConstructor // Lombok: 파라미터가 없는 기본 생성자를 자동 생성
@AllArgsConstructor // Lombok: 모든 필드를 파라미터로 받는 생성자를 자동 생성
@Builder // Lombok: 빌더 패턴을 사용하여 객체를 생성할 수 있도록 지원   
public class Baby {
    @Id // 기본키 지정  
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키 생성을 데이터베이스에 위임 (AUTO_INCREMENT)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 설정, 지연 로딩 방식 사용
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist // 엔티티가 데이터베이스에 저장되기 전에 자동으로 실행되는 메소드
    protected void onCreate() {
        createdAt = LocalDateTime.now(); // 생성 시간을 현재 시간으로 자동 설정
    }
} 