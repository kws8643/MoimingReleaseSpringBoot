package com.example.moiming_release.model.other;


import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class SessionStatusChangeDTO {

    private UUID sessionUuid;

    private List<UUID> unsentUserList; // sent 로 바꿔야함

    private List<UUID> unsentNmuList;

    private List<UUID> sentUserList; // unsent 로 바꿔야 함

    private List<UUID> sentNmuList;


}
