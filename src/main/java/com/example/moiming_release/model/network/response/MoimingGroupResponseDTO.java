package com.example.moiming_release.model.network.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoimingGroupResponseDTO {

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
    private LocalDateTime noticeCreatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
