package com.recovery.revenuerecovery.repository;

import com.recovery.revenuerecovery.model.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRecordRepository
        extends JpaRepository<PaymentRecord, Long> {

    Optional<PaymentRecord> findByPaymentId(
            String paymentId
    );

    Optional<PaymentRecord> findByRecoveryOrderId(
            String recoveryOrderId
    );
}