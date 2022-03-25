package com.example.moiming_release.repository;

import com.example.moiming_release.model.entity.GroupPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GroupPaymentRepository extends JpaRepository<GroupPayment, UUID> {
}
