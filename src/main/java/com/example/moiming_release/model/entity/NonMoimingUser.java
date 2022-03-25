package com.example.moiming_release.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@ToString(exclude = {"moimingSession"})
public class NonMoimingUser {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID uuid;

    private String nmuName;

    private Integer nmuPersonalCost;

    private boolean isNmuSent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // NonMoimingUser(N): MoimingSession(1) 관계 지정
    @ManyToOne
    @JoinColumn(name = "moiming_session_uuid")
    private MoimingSession moimingSession;


}
