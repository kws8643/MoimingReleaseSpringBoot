package com.example.moiming_release.model.network.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoimingUserResponseDTO {

    private UUID uuid;
    private String oauthUid;
    private String oauthType;
    private String userName;
    private String userEmail;
    private String phoneNumber;
    private String userPfImg;
    private String bankName;
    private String bankNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
