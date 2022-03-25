package com.example.moiming_release.model.other;

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
@Accessors(chain = true) // setter 들을 연속해서 쓸 수 있게 하는 LIB.
public class NotificationUserAndActivityDTO {

    private UUID userUuid;

    private UUID sentUserUuid;

    private UUID activityUuid;
}
