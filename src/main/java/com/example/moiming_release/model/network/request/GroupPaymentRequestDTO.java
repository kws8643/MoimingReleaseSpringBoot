package com.example.moiming_release.model.network.request;


import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupPaymentRequestDTO {

    private UUID groupUuid;

    private String paymentDate; // 보낼때는 상관 없는데, 받을때는 String 으로 옴

    private String paymentName;

    private int paymentCost;

    private boolean paymentType; //true: 수입, false: 지출


}
