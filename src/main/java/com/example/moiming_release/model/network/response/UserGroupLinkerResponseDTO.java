package com.example.moiming_release.model.network.response;

import com.example.moiming_release.model.other.MoimingGroupAndMembersDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGroupLinkerResponseDTO {

    private Long id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private MoimingGroupAndMembersDTO moimingGroupAndMembersDTO;


}
