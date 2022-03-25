package com.example.moiming_release.model.entity;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true) // setter 들을 연속해서 사용할 수 있도록
@ToString(exclude = {"moimingUser"})
public class PolicyAgree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer policyNumber; // 0: 이용약관 , 1: 만 14세 이상, 2: 개인정보, 3: 마케팅 수신동의
    private String policyInfo; // 간략 정보..
    private Boolean isAgreed;
    private LocalDate createdAt;
    private LocalDate updatedAt;


    // User(1) : PolicyAgree (N) 의 관계.
    @ManyToOne
    @JoinColumn(name = "moiming_user_uuid")
    private MoimingUser moimingUser;

}
