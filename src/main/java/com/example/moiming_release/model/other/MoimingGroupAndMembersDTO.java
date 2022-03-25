package com.example.moiming_release.model.other;

import com.example.moiming_release.model.entity.MoimingGroup;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.List;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class MoimingGroupAndMembersDTO {

    @SerializedName("moiming_group_dto")
    private MoimingGroup moimingGroupDto;

    private List<MoimingMembersDTO> moimingMembersList; // curUser도 포함되어 있음!


}
