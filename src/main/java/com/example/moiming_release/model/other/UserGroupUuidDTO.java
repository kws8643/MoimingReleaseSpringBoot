package com.example.moiming_release.model.other;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.lang.Nullable;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class UserGroupUuidDTO {

    @Nullable
    private String requestUserUuid; // 해당 Linking 을 만든 사람

    private String userUuid; // Linking 대상자

    private String groupUuid;

}
