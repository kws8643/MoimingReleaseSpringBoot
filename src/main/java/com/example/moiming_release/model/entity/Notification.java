package com.example.moiming_release.model.entity;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true) // setter 들을 연속해서 쓸 수 있게 하는 LIB.
@ToString(exclude = {"moimingUser"})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID sentUserUuid;

    private String sentActivity;

    private UUID sentGroupUuid;

    private UUID sentSessionUuid;

    private Integer msgType;

    private String msgText;

    private Boolean isRead;

    private LocalDateTime createdAt;

    // User(1) : Notification (N) 의 관계.
    @ManyToOne
    @JoinColumn(name = "moiming_user_uuid")
    private MoimingUser moimingUser;


}
