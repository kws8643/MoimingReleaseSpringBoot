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
@Data
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@ToString(exclude = {"moimingGroup"})
public class MoimingSession {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID uuid;

    private UUID sessionCreatorUuid;

    private Integer sessionType; // 0: 모금 / 1: 더치페

    private String sessionName;

    private Integer sessionMemberCnt;

    private Integer curSenderCnt;

    private Integer totalCost;

    private Integer singleCost; // 모금일 경우 1인당 금액 / 더치페이일 경우 원래 N빵 금액

    private Integer curCost;

    @Column(nullable = false, columnDefinition = "TINYTINT(1)")
    private Boolean isFinished;

    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // MoimingSession(N): Moiming Group(1) 관계 지정
    @ManyToOne
    @JoinColumn(name = "moiming_group_uuid")
    private MoimingGroup moimingGroup;


    // 포함하는 NMU 친구들을 가져올 수 있다.
    // MoimingSession(1): NMU(N)
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "moimingSession")
    private List<NonMoimingUser> nmuList;

    // UserSessionLinker 와의 관계.
    // MoimingSession(1) : UserSessionLinker (N)
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "moimingSession")
    private List<UserSessionLinker> userSessionList;


}
