package com.example.moiming_release.model.network.response;


import com.example.moiming_release.model.entity.MoimingSession;
import com.example.moiming_release.model.entity.MoimingUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSessionLinkerResponseDTO {

    private Integer personalCost;

    @Column(nullable = false, columnDefinition = "TINYTINT(1)")
    private Boolean isSent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 연결되어 있는 모이밍 유저를 전달
    private MoimingUser moimingUser;

}
