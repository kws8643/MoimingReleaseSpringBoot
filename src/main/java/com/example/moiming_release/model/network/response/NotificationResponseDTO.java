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
public class NotificationResponseDTO {

    private UUID sentUserUuid;

    private String sentActivity;

    private UUID sentGroupUuid;

    private UUID sentSessionUuid;

    private Integer msgType;

    private String msgText;

    private Boolean isRead;

    private LocalDateTime createdAt;

}
