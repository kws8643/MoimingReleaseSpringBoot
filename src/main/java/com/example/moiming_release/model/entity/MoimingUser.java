package com.example.moiming_release.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true) // setter 들을 연속해서 쓸 수 있게 하는 LIB.
@ToString(exclude = {"userGroupList"})
public class MoimingUser {

    /**
     * String bankName 은행별 코드 이름
     * KB = 카카오뱅크
     * NH = 농협은행
     * IBK = 기업은행
     * HN = 하나은행
     * WR= 우리은행
     * KM = 국민은행
     * SC = SC제일은행
     * DK = 대구은행
     * BS = 부산은행
     * KJ = 광주은행
     * SM = 새마을 금고
     * KN = 경남은행
     * JB = 전북은행
     * JJ = 제주은행
     * SU = 산업은행
     * SH = 신한은행
     * SHU = 신협은행
     * SUH = 수협은행
     * CT = 시티은행
     * K = 케이뱅크
     * TS = 토스뱅크
     * WC = 우체국
     */


    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
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

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "moimingUser")
    private List<UserGroupLinker> userGroupList;

    // UserSession List 도 불러올 수 있는데 굳이 여기서 불러올 필요는 없고, 해당 그룹으로 넘어가면 Search 로 가져온다.

}

