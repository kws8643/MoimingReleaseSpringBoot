package com.example.moiming_release.repository;

import com.example.moiming_release.model.entity.MoimingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MoimingSessionRepository extends JpaRepository<MoimingSession, UUID> {

    Optional<List<MoimingSession>> findByMoimingGroupUuid(UUID groupUuid);

    Optional<List<MoimingSession>> findByMoimingGroupUuidAndAndSessionType(UUID groupUuid, Integer sessionType);
}
