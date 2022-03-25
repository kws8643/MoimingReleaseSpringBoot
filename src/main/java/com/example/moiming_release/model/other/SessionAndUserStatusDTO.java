package com.example.moiming_release.model.other;

import com.example.moiming_release.model.entity.MoimingSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Column;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class SessionAndUserStatusDTO {

    private MoimingSession moimingSession;

    private String creatorName;

    private Integer curUserStatus; //0,1,2,3 으로 나뉜다

    private Integer curUserCost;

    // 1. 내가 주인인가? 0 = 내 정산
    // 2. 진행중인 정산인가?
    // 3. 내가 보내야 하는가? 1 = 송금 필요 2 = 송금 완료 3 = 송금 확인 중 4 = 미참여
    // 4. 얼마를 보내야 하는가?
}
