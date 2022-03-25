package com.example.moiming_release.model.network.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NonMoimingUserResponseDTO {

    private UUID uuid;

    private String nmuName;

    private Integer nmuPersonalCost;

    private Boolean isNmuSent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
