package com.example.moiming_release.model.entity;


import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@ToString(exclude = {"moimingUser", "moimingSession"})
public class UserSessionLinker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer personalCost;

    @Column(nullable = false, columnDefinition = "TINYTINT(1)")
    private boolean isSent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    // Foreign Key 관계에 의해 자동으로 MoimingUser 을 반환.
    // UserSessionLinker(N) : User (1) 의 관계.
    @ManyToOne
    @JoinColumn(name = "moiming_user_uuid")
    private MoimingUser moimingUser;

    // UserSessionLinker(N) : Session (1)
    @ManyToOne
    @JoinColumn(name = "moiming_session_uuid")
    private MoimingSession moimingSession;
}
