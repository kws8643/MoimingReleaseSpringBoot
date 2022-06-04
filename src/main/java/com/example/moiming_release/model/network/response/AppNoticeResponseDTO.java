package com.example.moiming_release.model.network.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppNoticeResponseDTO {

    private UUID uuid;

    private String noticeTitle;

    private String noticeInfo;

    private Boolean isUrlLinked;

    private String noticeUrl;

    private Boolean isOpen;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
