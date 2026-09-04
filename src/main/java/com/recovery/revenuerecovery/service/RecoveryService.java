package com.recovery.revenuerecovery.service;

import com.recovery.revenuerecovery.model.AIRecoveryDecision;
import com.recovery.revenuerecovery.model.PaymentRecord;
import com.recovery.revenuerecovery.model.RecoveryDecision;
import com.recovery.revenuerecovery.repository.PaymentRecordRepository;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RecoveryService {

    private final PaymentRecordRepository paymentRecordRepository;

private final RecoveryAuditService recoveryAuditService;

public RecoveryService(
        PaymentRecordRepository paymentRecordRepository,
        RecoveryAuditService recoveryAuditService) {

    this.paymentRecordRepository =
            paymentRecordRepository;

    this.recoveryAuditService =
            recoveryAuditService;
}

    public RecoveryDecision recoverPayment(
            JSONObject webhook,
            AIRecoveryDecision aiDecision) {

        System.out.println();
        System.out.println("====================================");
        System.out.println("💰 REVENUE RECOVERY ENGINE");
        System.out.println("====================================");


        /*
         * =====================================
         * 1. EXTRACT PAYMENT INFORMATION
         * =====================================
         */

        JSONObject payload =
                webhook.optJSONObject("payload");

        if (payload == null) {

            System.out.println(
                    "⚠️ No payload found"
            );

            return null;
        }

        JSONObject paymentContainer =
                payload.optJSONObject("payment");

        if (paymentContainer == null) {

            System.out.println(
                    "⚠️ No payment information found"
            );

            return null;
        }

        JSONObject payment =
                paymentContainer.optJSONObject("entity");

        if (payment == null) {

            System.out.println(
                    "⚠️ Payment entity not found"
            );

            return null;
        }


        /*
         * =====================================
         * 2. PAYMENT DETAILS
         * =====================================
         */

        String paymentId =
                payment.optString(
                        "id",
                        "unknown"
                );

        String orderId =
                payment.optString(
                        "order_id",
                        "unknown"
                );

        long amount =
                payment.optLong(
                        "amount",
                        0
                );

        String currency =
                payment.optString(
                        "currency",
                        "INR"
                );

        String method =
                payment.optString(
                        "method",
                        "unknown"
                );


        System.out.println(
                "Payment ID     : " + paymentId
        );

        System.out.println(
                "Order ID       : " + orderId
        );

        System.out.println(
                "Amount         : ₹" +
                        (amount / 100.0)
        );

        System.out.println(
                "Payment Method : " + method
        );


        /*
         * =====================================
         * 3. USE AI DECISION
         * =====================================
         */

        if (aiDecision == null) {

            System.out.println(
                    "⚠️ AI decision unavailable"
            );

            return null;
        }


        System.out.println();
        System.out.println(
                "🤖 USING AI RECOVERY DECISION"
        );


        RecoveryDecision decision =
                new RecoveryDecision(

                        aiDecision.getFailureType(),

                        aiDecision.getRecommendedAction(),

                        aiDecision
                                .getRecoveryProbability(),

                        aiDecision.getPriority(),

                        aiDecision.getReason()
                );


        /*
         * =====================================
         * 4. DISPLAY DECISION
         * =====================================
         */

        System.out.println();
        System.out.println(
                "Failure Type       : " +
                        aiDecision.getFailureType()
        );

        System.out.println(
                "Root Cause         : " +
                        aiDecision.getRootCause()
        );

        System.out.println(
                "Recovery Action    : " +
                        aiDecision.getRecommendedAction()
        );

        System.out.println(
                "Probability        : " +
                        aiDecision.getRecoveryProbability() +
                        "%"
        );

        System.out.println(
                "Priority           : " +
                        aiDecision.getPriority()
        );

        System.out.println(
                "Reason             : " +
                        aiDecision.getReason()
        );


        /*
         * =====================================
         * 5. CHECK EXISTING PAYMENT
         * =====================================
         */

        PaymentRecord existingRecord =
                paymentRecordRepository
                        .findByPaymentId(paymentId)
                        .orElse(null);


        if (existingRecord != null) {

            System.out.println();
            System.out.println(
                    "⚠️ PAYMENT ALREADY EXISTS"
            );

            System.out.println(
                    "Payment ID: " +
                            paymentId
            );

            System.out.println(
                    "Skipping duplicate database insert."
            );

            System.out.println(
                    "===================================="
            );

            return decision;
        }


        /*
         * =====================================
         * 6. CREATE PAYMENT RECORD
         * =====================================
         */

        PaymentRecord record =
                new PaymentRecord();


        record.setPaymentId(
                paymentId
        );

        record.setOrderId(
                orderId
        );

        record.setAmount(
                amount
        );

        record.setCurrency(
                currency
        );

        record.setPaymentMethod(
                method
        );

        record.setStatus(
                "FAILED"
        );

        record.setFailureType(
                aiDecision.getFailureType()
        );

        record.setRecoveryAction(
                aiDecision.getRecommendedAction()
        );

        record.setRecoveryProbability(
                aiDecision.getRecoveryProbability()
        );

        record.setPriority(
                aiDecision.getPriority()
        );

        record.setReason(
                "Root Cause: " +
                        aiDecision.getRootCause() +
                        " | " +
                        aiDecision.getReason()
        );

        record.setCreatedAt(
                LocalDateTime.now()
        );


        /*
         * =====================================
         * 7. SAVE TO H2
         * =====================================
         */

        paymentRecordRepository.save(
                record
        );

        recoveryAuditService.log(
        record,
        "PAYMENT_FAILED",
        null,
        "Payment failure detected by Razorpay webhook"
);

recoveryAuditService.log(
        record,
        "AI_ANALYZED",
        aiDecision.getRecommendedAction(),
        "Failure Type: " +
                aiDecision.getFailureType() +
                " | Root Cause: " +
                aiDecision.getRootCause() +
                " | Recovery Probability: " +
                aiDecision.getRecoveryProbability() +
                "% | Priority: " +
                aiDecision.getPriority() +
                " | Reason: " +
                aiDecision.getReason()
);


        System.out.println();
        System.out.println(
                "💾 AI PAYMENT RECORD SAVED"
        );

        System.out.println(
                "Database : H2"
        );

        System.out.println(
                "Payment ID : " +
                        paymentId
        );

        System.out.println(
                "Status : FAILED"
        );

        System.out.println(
                "AI Decision : SAVED"
        );

        System.out.println(
                "===================================="
        );

        System.out.println();


        return decision;
    }
}