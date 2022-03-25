package com.example.moiming_release.model.network.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyAgreeRequestDTO {

    private Integer policyNumber; // 0: 이용약관 , 1: 만 14세 이상, 2: 개인정보, 3: 마케팅 수신동의

    private String policyInfo; // 간략 정보..

    private Boolean isAgreed;

}
