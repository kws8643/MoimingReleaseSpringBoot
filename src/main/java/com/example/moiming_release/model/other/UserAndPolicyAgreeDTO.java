package com.example.moiming_release.model.other;


import com.example.moiming_release.model.network.request.PolicyAgreeRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class UserAndPolicyAgreeDTO {

    private UUID userUuid;

    private List<PolicyAgreeRequestDTO> userAgreeList;

}
