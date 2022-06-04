package com.example.moiming_release.repository;

import com.example.moiming_release.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 내가 총무일 때 수신한 msgType 2 들이 있는지 확인하기 위함.
    Optional<List<Notification>> findByMoimingUserUuidAndSentSessionUuidAndMsgType(UUID userUuid, UUID sentSessionUuid, Integer msgType);

    // 내가 총무일 때 이미 송신한 msgType 1들이 있는지 확인하기 위함.
    Optional<List<Notification>> findBySentSessionUuidAndMoimingUserUuidAndMsgType(UUID sentSessionUuid, UUID receiverUuid, Integer msgType);

    Optional<List<Notification>> findByMoimingUserUuid(UUID userUuid);

    Optional<Notification> findBySentUserUuidAndSentSessionUuidAndMsgType(UUID sentUserUuid, UUID sentSessionUuid, Integer msgType);

    // 해당 그룹에 대한 알림만 가져오기 위해
    Optional<List<Notification>> findByMoimingUserUuidAndSentGroupUuid(UUID userUuid, UUID sentGroupUuid);

    // 해당 정산에 대한 알림만 가져오기 위해
    Optional<List<Notification>> findByMoimingUserUuidAndSentSessionUuid(UUID userUuid, UUID sentSessionUuid);


    // Notifiaction 삭제용
    Optional<List<Notification>> findBySentSessionUuid(UUID sessionUuid);

}
