package com.example.moiming_release.model.network.response;

import com.example.moiming_release.model.entity.NonMoimingUser;
import com.example.moiming_release.model.entity.UserSessionLinker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoimingSessionResponseDTO {
    private UUID uuid;
    private UUID sessionCreatorUuid;
    private Integer sessionType; // 0: 모금 / 1: 더치페
    private String sessionName;
    private Integer sessionMemberCnt;
    private Integer curSenderCnt;
    private Integer totalCost;
    private Integer singleCost;
    private Integer curCost;

    private List<NonMoimingUser> nmuList;
    private List<UserSessionLinker> userSessionList;


    @Column(nullable = false, columnDefinition = "TINYTINT(1)")
    private Boolean isFinished;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
