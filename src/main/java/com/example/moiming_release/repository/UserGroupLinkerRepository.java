package com.example.moiming_release.repository;

import com.example.moiming_release.model.entity.MoimingGroup;
import com.example.moiming_release.model.entity.MoimingUser;
import com.example.moiming_release.model.entity.UserGroupLinker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserGroupLinkerRepository extends JpaRepository<UserGroupLinker, Long> {

    Optional<UserGroupLinker> findByMoimingUserUuidAndMoimingGroupUuid(UUID userUuid, UUID groupUuid);

    Optional<List<UserGroupLinker>> findByMoimingUserUuid(UUID userUuid);

}
