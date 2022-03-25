package com.example.moiming_release.repository;

import com.example.moiming_release.model.entity.UserGroupLinker;
import com.example.moiming_release.model.entity.UserSessionLinker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionLinkerRepository extends JpaRepository<UserSessionLinker, Long> {

    Optional<List<UserSessionLinker>> findByMoimingSession_Uuid(UUID sessionUuid);

    Optional<UserSessionLinker> findByMoimingSessionUuidAndMoimingUserUuid(UUID sessionUuid, UUID userUuid);

}
