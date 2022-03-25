package com.example.moiming_release.repository;

import com.example.moiming_release.model.entity.MoimingUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MoimingUserRepository extends JpaRepository<MoimingUser, UUID> {

    Optional<MoimingUser> findByOauthUidAndAndOauthType(String oauthUid, String oauthType);
}
