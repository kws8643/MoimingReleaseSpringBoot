package com.example.moiming_release.model.network.request;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MoimingGroupRequestDTO {

    private String groupName;

    private String groupInfo;

    private UUID groupCreatorUuid;

    private String bgImg;

    private Integer groupMemberCnt;


    public String toString() {

        String jsonType = "{\n"
                + "groupName:\"" + groupName + "\""
                + "\ngroupInfo:\"" + groupInfo + "\""
                + "\ngroupCreatorUuid:\"" + groupCreatorUuid + "\""
                + "\ngroupMemberCnt:\"" + groupMemberCnt + "\""
                + "\n}";

        return jsonType;
    }


}
