package com.recovery.revenuerecovery.controller;

import com.recovery.revenuerecovery.model.PaymentRecord;
import com.recovery.revenuerecovery.repository.PaymentRecordRepository;
import com.recovery.revenuerecovery.service.PaymentService;
import com.recovery.revenuerecovery.service.RecoveryAuditService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRecordRepository paymentRecordRepository;
    private final RecoveryAuditService recoveryAuditService;

    public PaymentController(
            PaymentService paymentService,
            PaymentRecordRepository paymentRecordRepository,
            RecoveryAuditService recoveryAuditService) {

        this.paymentService = paymentService;
        this.paymentRecordRepository = paymentRecordRepository;
        this.recoveryAuditService = recoveryAuditService;
    }


    /*
     * =====================================
     * CREATE PAYMENT ORDER
     * =====================================
     */

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            @RequestParam long amount,
            @RequestParam(defaultValue = "INR") String currency) {

        try {

            String order =
                    paymentService.createOrder(
                            amount,
                            currency
                    );

            return ResponseEntity.ok(order);

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Unable to create Razorpay order: "
                                    + e.getMessage()
                    );
        }
    }


    /*
     * =====================================
     * VERIFY NORMAL PAYMENT
     * =====================================
     */

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestParam String orderId,
            @RequestParam String paymentId,
            @RequestParam String signature) {

        boolean verified =
                paymentService.verifyPayment(
                        orderId,
                        paymentId,
                        signature
                );

        if (verified) {

            return ResponseEntity.ok(
                    "Payment verified successfully"
            );
        }

        return ResponseEntity
                .badRequest()
                .body(
                        "Payment verification failed"
                );
    }


    /*
     * =====================================
     * VERIFY RECOVERY PAYMENT
     * =====================================
     *
     * This endpoint is called after the
     * merchant/customer successfully pays
     * through the recovery Razorpay Checkout.
     *
     * It:
     *
     * 1. Verifies Razorpay signature
     * 2. Finds the original failed payment
     * 3. Stores the recovery payment ID
     * 4. Marks payment as RECOVERED
     * 5. Records recovery outcome
     * 6. Writes recovery audit events
     *
     */

    @PostMapping("/verify-recovery")
    public ResponseEntity<?> verifyRecoveryPayment(
            @RequestParam String orderId,
            @RequestParam String paymentId,
            @RequestParam String signature) {

        try {

            /*
             * =====================================
             * 1. VERIFY RAZORPAY SIGNATURE
             * =====================================
             */

            boolean verified =
                    paymentService.verifyPayment(
                            orderId,
                            paymentId,
                            signature
                    );


            if (!verified) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Recovery payment verification failed"
                        );
            }


            /*
             * =====================================
             * 2. FIND ORIGINAL PAYMENT
             * =====================================
             */

            PaymentRecord record =
                    paymentRecordRepository
                            .findByRecoveryOrderId(orderId)
                            .orElse(null);


            if (record == null) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Original payment record not found"
                        );
            }


            /*
             * =====================================
             * 3. PREVENT DUPLICATE RECOVERY
             * =====================================
             */

            if ("RECOVERED".equalsIgnoreCase(
                    record.getStatus())) {

                return ResponseEntity.ok(
                        "Payment was already recovered"
                );
            }


            /*
             * =====================================
             * 4. SAVE RECOVERY PAYMENT
             * =====================================
             */

            record.setRecoveryPaymentId(
                    paymentId
            );


            /*
             * =====================================
             * 5. UPDATE RECOVERY STATUS
             * =====================================
             */

            record.setStatus(
                    "RECOVERED"
            );


            /*
             * =====================================
             * 6. UPDATE RECOVERY OUTCOME
             * =====================================
             */

            record.setRecoveryOutcome(
                    "RECOVERED"
            );


            /*
             * =====================================
             * 7. SAVE DATABASE RECORD
             * =====================================
             */

            paymentRecordRepository.save(
                    record
            );


            /*
             * =====================================
             * 8. AUDIT: RECOVERY VERIFIED
             * =====================================
             */

            recoveryAuditService.log(
                    record,
                    "RECOVERY_VERIFIED",
                    record.getRecoveryAction(),
                    "Razorpay recovery payment signature verified successfully. " +
                            "Recovery Payment ID: " +
                            paymentId +
                            " | Recovery Order ID: " +
                            orderId
            );


            /*
             * =====================================
             * 9. AUDIT: RECOVERED
             * =====================================
             */

            recoveryAuditService.log(
                    record,
                    "RECOVERED",
                    record.getRecoveryAction(),
                    "Revenue successfully recovered. " +
                            "Amount: " +
                            (record.getAmount() / 100.0) +
                            " " +
                            record.getCurrency()
            );


            /*
             * =====================================
             * 10. LOG SUCCESS
             * =====================================
             */

            System.out.println();

            System.out.println(
                    "===================================="
            );

            System.out.println(
                    "🎉 PAYMENT RECOVERED"
            );

            System.out.println(
                    "Original Payment ID: " +
                            record.getPaymentId()
            );

            System.out.println(
                    "Recovery Payment ID: " +
                            record.getRecoveryPaymentId()
            );

            System.out.println(
                    "Recovery Order ID: " +
                            record.getRecoveryOrderId()
            );

            System.out.println(
                    "Recovery Attempts: " +
                            record.getRecoveryAttempts()
            );

            System.out.println(
                    "Status: " +
                            record.getStatus()
            );

            System.out.println(
                    "Outcome: " +
                            record.getRecoveryOutcome()
            );

            System.out.println(
                    "===================================="
            );


            /*
             * =====================================
             * 11. RETURN SUCCESS
             * =====================================
             */

            return ResponseEntity.ok(
                    "Payment recovered successfully"
            );


        } catch (Exception e) {

            System.out.println();

            System.out.println(
                    "❌ Recovery verification error: "
                            + e.getMessage()
            );


            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Unable to verify recovery payment"
                    );
        }
    }
}