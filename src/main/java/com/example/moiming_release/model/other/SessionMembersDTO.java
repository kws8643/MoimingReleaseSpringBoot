package com.example.moiming_release.model.other;

import com.example.moiming_release.model.network.response.NonMoimingUserResponseDTO;
import com.example.moiming_release.model.network.response.UserSessionLinkerResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class SessionMembersDTO {

    private MoimingMembersDTO sessionCreatorInfo;

    private List<UserSessionLinkerResponseDTO> sessionMoimingMemberList;

    private List<NonMoimingUserResponseDTO> sessionNmuList;
}
