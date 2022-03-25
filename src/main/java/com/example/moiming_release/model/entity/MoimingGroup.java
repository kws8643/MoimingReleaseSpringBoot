package com.example.moiming_release.model.entity;


import com.example.moiming_release.model.other.MoimingMembersDTO;
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
@Accessors(chain = true) // setter 들을 연속해서 사용할 수 있도록
@ToString(exclude = {"groupUserList", "moimingSessionList"})
public class MoimingGroup {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID uuid;

    private String groupName;

    private String groupPfImg;

    private String groupInfo;

    private Integer groupMemberCnt;

    private String bgImg;

    private UUID groupCreatorUuid;

    private Integer groupPayment;

    private String notice;

    private UUID noticeCreatorUuid;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime noticeCreatedAt;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "moimingGroup")
    private List<UserGroupLinker> groupUserList;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "moimingGroup")
    private List<MoimingSession> moimingSessionList;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "moimingGroup")
    private List<GroupPayment> paymentList;

}
