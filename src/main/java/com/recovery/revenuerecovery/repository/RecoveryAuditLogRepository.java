package com.recovery.revenuerecovery.repository;

import com.recovery.revenuerecovery.model.RecoveryAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecoveryAuditLogRepository
        extends JpaRepository<RecoveryAuditLog, Long> {

    List<RecoveryAuditLog> findByPaymentIdOrderByCreatedAtAsc(
            String paymentId
    );
}