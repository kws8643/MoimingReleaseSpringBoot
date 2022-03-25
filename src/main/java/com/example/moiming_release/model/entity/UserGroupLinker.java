package com.example.moiming_release.model.entity;

import lombok.*;
import lombok.experimental.Accessors;
import net.bytebuddy.dynamic.loading.InjectionClassLoader;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
//@ToString(exclude = {"moimingUser", "moimingGroup"})
public class UserGroupLinker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    // Foreign Key 관계에 의해 자동으로 MoimingUser 을 반환.
    // UserGroupLinker(N) : User (1) 의 관계.
    @ManyToOne
    @JoinColumn(name = "moiming_user_uuid")
    private MoimingUser moimingUser;

    // Moiming Grouop 역시 동일하게 들어감.
    // UserGroupLinker(N) : Group (1)
    @ManyToOne
    @JoinColumn(name = "moiming_group_uuid")
    private MoimingGroup moimingGroup;

}
