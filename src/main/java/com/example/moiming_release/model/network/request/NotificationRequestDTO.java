package com.example.moiming_release.model.network.request;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestDTO {

    private UUID toUserUuid;

    private UUID sentUserUuid;

    private String sentActivity;

    private UUID sentGroupUuid;

    private UUID sentSessionUuid;

    private Integer msgType;

    private String msgText;


}
