package com.recovery.revenuerecovery.service;

import com.recovery.revenuerecovery.model.PaymentRecord;
import com.recovery.revenuerecovery.model.RecoveryAuditLog;
import com.recovery.revenuerecovery.repository.RecoveryAuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RecoveryAuditService {

    private final RecoveryAuditLogRepository auditLogRepository;

    public RecoveryAuditService(
            RecoveryAuditLogRepository auditLogRepository) {

        this.auditLogRepository = auditLogRepository;
    }

    public void log(
            PaymentRecord record,
            String eventType,
            String action,
            String details) {

        if (record == null) {
            return;
        }

        RecoveryAuditLog log =
                new RecoveryAuditLog();

        log.setPaymentId(
                record.getPaymentId()
        );

        log.setEventType(
                eventType
        );

        log.setAction(
                action
        );

        log.setAttempt(
                record.getRecoveryAttempts()
        );

        log.setAmount(
                record.getAmount()
        );

        log.setCurrency(
                record.getCurrency()
        );

        log.setDetails(
                details
        );

        log.setCreatedAt(
                LocalDateTime.now()
        );

        auditLogRepository.save(log);

        System.out.println(
                "📝 AUDIT: " +
                        eventType +
                        " | Payment: " +
                        record.getPaymentId()
        );
    }
}