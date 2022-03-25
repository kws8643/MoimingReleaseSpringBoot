package com.example.moiming_release.model.entity;

import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true) // setter 들을 연속해서 사용할 수 있도록
@ToString(exclude = {"moimingGroup"})
public class GroupPayment {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID uuid;

    private LocalDate paymentDate;

    private String paymentName;

    private int paymentCost;

    private boolean paymentType; //true: 수입, false: 지출

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
    // GroupPayment(N): Moiming Group(1) 관계 지정
    @ManyToOne
    @JoinColumn(name = "moiming_group_uuid")
    private MoimingGroup moimingGroup;


}
