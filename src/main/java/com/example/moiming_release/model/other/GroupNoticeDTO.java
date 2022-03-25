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
@Accessors(chain = true)
public class GroupNoticeDTO {

    private UUID groupUuid;

    private UUID noticeCreatorUuid;

    private String noticeInfo;

}
