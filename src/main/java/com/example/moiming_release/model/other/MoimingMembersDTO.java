package com.example.moiming_release.model.other;

import com.example.moiming_release.model.entity.MoimingUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true) // setter 들을 연속해서 쓸 수 있게 하는 LIB.
public class MoimingMembersDTO {

    private UUID uuid;

    private String oauthUid;

    private String userName;

    private String userPfImg;

    private String bankName;

    private String bankNumber;

    public String toString(){

        String jsonType = "{\n"
                + "uuid:\"" + uuid.toString() + "\""
                + "\nuserName:\"" + userName + "\""
                + "\nbankName:\"" + bankName + "\""
                + "\n}";

        return jsonType;
    }

}
