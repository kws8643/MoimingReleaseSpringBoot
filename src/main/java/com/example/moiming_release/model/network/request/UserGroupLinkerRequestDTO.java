package com.example.moiming_release.model.network.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserGroupLinkerRequestDTO {

    private UUID groupUuid;

    private Map<Integer, UUID> membersUuid;

}
