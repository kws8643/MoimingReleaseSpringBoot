package com.example.moiming_release.model.network.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupPaymentResponseDTO {

    private UUID uuid;
    private String paymentDate;
    private String paymentName;
    private int paymentCost;
    private boolean paymentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
