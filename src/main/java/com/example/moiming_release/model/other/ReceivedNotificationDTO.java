package com.example.moiming_release.model.other;

import com.example.moiming_release.model.network.response.NotificationResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class ReceivedNotificationDTO {

    private String sentUserName;

    private String sentGroupName;

    private String sentSessionName;

    private NotificationResponseDTO notification;

}
