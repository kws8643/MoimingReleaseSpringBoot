package com.example.moiming_release.model.other;

import com.example.moiming_release.model.entity.MoimingGroup;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class MoimingGroupEditInfoDto {

    @SerializedName("group_uuid")
    private UUID groupUuid;

    @SerializedName("group_name")
    private String groupName;

    @SerializedName("group_info")
    private String groupInfo;

    @SerializedName("group_pf_img")
    private String groupPfImg;


}
