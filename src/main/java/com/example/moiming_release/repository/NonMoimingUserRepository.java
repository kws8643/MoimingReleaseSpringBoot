package com.example.moiming_release.repository;

import com.example.moiming_release.model.entity.NonMoimingUser;
import com.example.moiming_release.model.entity.UserSessionLinker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NonMoimingUserRepository extends JpaRepository<NonMoimingUser, UUID> {

    Optional<List<NonMoimingUser>> findByMoimingSession_Uuid(UUID sessionUuid);

    Optional<NonMoimingUser> findByMoimingSessionUuidAndUuid(UUID sessionUuid, UUID nmuUuid);

}
