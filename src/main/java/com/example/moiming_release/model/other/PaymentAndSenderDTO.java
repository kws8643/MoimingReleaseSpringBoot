package com.example.moiming_release.model.other;


import com.example.moiming_release.model.network.request.GroupPaymentRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class PaymentAndSenderDTO {

    private UUID sentUserUuid;

    private GroupPaymentRequestDTO paymentData;


}
