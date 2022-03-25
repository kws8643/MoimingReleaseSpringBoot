package com.example.moiming_release.model.other;


import com.example.moiming_release.model.network.request.MoimingGroupRequestDTO;
import com.example.moiming_release.model.network.request.MoimingSessionRequestDTO;
import com.example.moiming_release.model.network.request.UserSessionLinkerRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class GroupAndSessionCreationDTO {

    // 초대하는 그룹원들의 uuid
    private List<UUID> membersUuidList; // Linker 들 형성용

    private List<UserSessionLinkerRequestDTO> usDataList;

    private MoimingGroupRequestDTO groupRequest;

    private MoimingSessionRequestDTO sessionRequest;

}
