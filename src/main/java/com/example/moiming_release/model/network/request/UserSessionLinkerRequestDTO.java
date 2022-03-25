package com.example.moiming_release.model.network.request;


import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSessionLinkerRequestDTO {

    private UUID sessionUuid; // 만드는 세션.

    private UUID moimingUserUuid;

    @Column(nullable = false, columnDefinition = "TINYTINT(1)")
    private Boolean isMoimingUser;

    private String userName;

    private Integer personalCost;

    public String toString() {

        String jsonType = "{\n"
                + "sessionUuid:\"" + sessionUuid + "\""
                + "\nmoimingUserUuid:\"" + moimingUserUuid + "\""
                + "\nisMoimingUser:\"" + isMoimingUser + "\""
                + "\nuserName:\"" + userName + "\""
                + "\npersonalCost:\"" + personalCost + "\""
                + "\n}";

        return jsonType;
    }

}
