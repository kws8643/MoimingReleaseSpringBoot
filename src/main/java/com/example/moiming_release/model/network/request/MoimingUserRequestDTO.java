package com.example.moiming_release.model.network.request;


import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MoimingUserRequestDTO {

    private UUID userUuid;
    private String oauthUid;
    private String oauthType;
    private String userName;
    private String userEmail;
    private String bankName;
    private String bankNumber;
    private String userPfImg;
    private String phoneNumber;


    public String toString() {

        String jsonType = "{\n"
                + "oauthType:\"" + oauthType + "\""
                + "\nuserName:\"" + userName + "\""
                + "\nuserEmail:\"" + userEmail + "\""
                + "\nuserPfImg:\"" + userPfImg + "\""
                + "\nphoneNumber:\"" + phoneNumber + "\""
                + "\n}";

        return jsonType;
    }

}
