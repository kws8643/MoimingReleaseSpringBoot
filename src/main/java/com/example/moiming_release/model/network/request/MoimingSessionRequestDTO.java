package com.example.moiming_release.model.network.request;


import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MoimingSessionRequestDTO {

    private UUID sessionCreatorUuid;

    private UUID moimingGroupUuid;

    private Integer sessionType;

    private Integer sessionCreatorCost;

    private String sessionName;

    private Integer sessionMemberCnt;

    private Integer totalCost;

    private Integer singleCost;

    public String toString() {

        String jsonType = "{\n"
                + "sessionCreatorUuid:\"" + sessionCreatorUuid.toString() + "\""
                + "\nmoimingGroupUuid:\"" + moimingGroupUuid + "\""
                + "\nsessionType:\"" + sessionType + "\""
                + "\nsessionName:\"" + sessionName + "\""
                + "\nsessionMemberCnt:\"" + sessionMemberCnt + "\""
                + "\ntotalCost:\"" + totalCost + "\""
                + "\n}";

        return jsonType;
    }


}
