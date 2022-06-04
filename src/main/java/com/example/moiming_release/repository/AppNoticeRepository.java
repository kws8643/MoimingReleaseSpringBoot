package com.example.moiming_release.repository;

import com.example.moiming_release.model.entity.AppNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppNoticeRepository extends JpaRepository<AppNotice, UUID> {

    Optional<List<AppNotice>> findByIsOpen(boolean isOpen);

}
