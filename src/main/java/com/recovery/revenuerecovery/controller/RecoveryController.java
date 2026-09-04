package com.recovery.revenuerecovery.controller;

import com.recovery.revenuerecovery.model.PaymentRecord;
import com.recovery.revenuerecovery.repository.PaymentRecordRepository;
import com.recovery.revenuerecovery.service.PaymentService;
import com.recovery.revenuerecovery.service.RecoveryAuditService;
import com.recovery.revenuerecovery.service.RecoveryPolicyService;

import org.json.JSONObject;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recovery")
@CrossOrigin
public class RecoveryController {

    private final PaymentRecordRepository paymentRecordRepository;

    private final PaymentService paymentService;

    private final RecoveryPolicyService recoveryPolicyService;

    private final RecoveryAuditService recoveryAuditService;


    public RecoveryController(
            PaymentRecordRepository paymentRecordRepository,
            PaymentService paymentService,
            RecoveryPolicyService recoveryPolicyService,
            RecoveryAuditService recoveryAuditService) {

        this.paymentRecordRepository =
                paymentRecordRepository;

        this.paymentService =
                paymentService;

        this.recoveryPolicyService =
                recoveryPolicyService;

        this.recoveryAuditService =
                recoveryAuditService;
    }


    @PostMapping("/{paymentId}")
    public ResponseEntity<?> recoverPayment(
            @PathVariable String paymentId) {

        try {

            /*
             * =====================================
             * 1. FIND PAYMENT
             * =====================================
             */

            PaymentRecord record =
                    paymentRecordRepository
                            .findByPaymentId(paymentId)
                            .orElse(null);


            if (record == null) {

                return ResponseEntity
                        .notFound()
                        .build();
            }


            /*
             * =====================================
             * 2. ALREADY RECOVERED CHECK
             * =====================================
             */

            if ("RECOVERED".equalsIgnoreCase(
                    record.getStatus())) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "success", false,

                                        "message",
                                        "Payment has already been recovered",

                                        "outcome",
                                        "RECOVERED"
                                )
                        );
            }


            /*
             * =====================================
             * 3. POLICY ENGINE
             * =====================================
             */

            System.out.println();
            System.out.println("====================================");
            System.out.println("🛡️ RECOVERY POLICY ENGINE");
            System.out.println("====================================");

            System.out.println(
                    "Payment ID       : " +
                            paymentId
            );

            System.out.println(
                    "AI Action        : " +
                            record.getRecoveryAction()
            );

            System.out.println(
                    "Attempts         : " +
                            record.getRecoveryAttempts()
            );

            System.out.println(
                    "Maximum Attempts : " +
                            recoveryPolicyService
                                    .getMaxRecoveryAttempts()
            );


            /*
             * =====================================
             * 4. CHECK POLICY
             * =====================================
             */

            boolean allowed =
                    recoveryPolicyService
                            .canRecover(record);


            if (!allowed) {

                String reason;


                if (record.getRecoveryAttempts()
                        >= recoveryPolicyService
                        .getMaxRecoveryAttempts()) {

                    reason =
                            "Maximum recovery attempts reached";

                } else if (
                        record.getRecoveryAction() == null
                                || record.getRecoveryAction()
                                .isBlank()) {

                    reason =
                            "No valid recovery action available";

                } else {

                    reason =
                            "Recovery action is not permitted by policy";
                }


                recoveryPolicyService
                        .stopRecovery(
                                record,
                                reason
                        );


                paymentRecordRepository.save(
                        record
                );


                /*
                 * AUDIT: POLICY STOPPED
                 */

                recoveryAuditService.log(
                        record,
                        "POLICY_STOPPED",
                        record.getRecoveryAction(),
                        reason
                );


                System.out.println();
                System.out.println(
                        "🛑 RECOVERY STOPPED"
                );

                System.out.println(
                        "Reason: " + reason
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


                return ResponseEntity.ok(
                        Map.of(
                                "success", false,

                                "paymentId",
                                record.getPaymentId(),

                                "attempts",
                                record.getRecoveryAttempts(),

                                "maxAttempts",
                                recoveryPolicyService
                                        .getMaxRecoveryAttempts(),

                                "outcome",
                                "STOPPED",

                                "message",
                                reason
                        )
                );
            }


            /*
             * =====================================
             * 5. POLICY APPROVED
             * =====================================
             */

            System.out.println();
            System.out.println(
                    "✅ POLICY APPROVED"
            );

            System.out.println(
                    "Recovery action allowed: " +
                            record.getRecoveryAction()
            );


            /*
             * AUDIT: POLICY APPROVED
             */

            recoveryAuditService.log(
                    record,
                    "POLICY_APPROVED",
                    record.getRecoveryAction(),
                    "Recovery action approved by policy engine. " +
                            "Attempt " +
                            record.getRecoveryAttempts() +
                            "/" +
                            recoveryPolicyService
                                    .getMaxRecoveryAttempts()
            );


            /*
             * =====================================
             * 6. REGISTER ATTEMPT
             * =====================================
             */

            recoveryPolicyService
                    .registerRecoveryAttempt(
                            record
                    );


            /*
             * =====================================
             * 7. CREATE NEW RAZORPAY ORDER
             * =====================================
             */

            System.out.println();
            System.out.println(
                    "🚀 STARTING PAYMENT RECOVERY"
            );

            System.out.println(
                    "Original Payment ID: " +
                            paymentId
            );

            System.out.println(
                    "Recovery Action: " +
                            record.getRecoveryAction()
            );

            System.out.println(
                    "Recovery Attempt: " +
                            record.getRecoveryAttempts()
            );


            /*
             * Amount is already stored in paise.
             */

            String orderJson =
                    paymentService.createOrder(
                            record.getAmount(),
                            record.getCurrency()
                    );


            JSONObject order =
                    new JSONObject(orderJson);


            String recoveryOrderId =
                    order.getString("id");


            /*
             * =====================================
             * 8. SAVE RECOVERY ORDER
             * =====================================
             */

            record.setRecoveryOrderId(
                    recoveryOrderId
            );

            paymentRecordRepository.save(
                    record
            );


            /*
             * AUDIT: RECOVERY ORDER CREATED
             */

            recoveryAuditService.log(
                    record,
                    "RECOVERY_ORDER_CREATED",
                    record.getRecoveryAction(),
                    "Razorpay recovery order created: " +
                            recoveryOrderId
            );


            System.out.println(
                    "Recovery Order ID: " +
                            recoveryOrderId
            );

            System.out.println(
                    "===================================="
            );


            /*
             * =====================================
             * 9. RETURN CHECKOUT INFORMATION
             * =====================================
             */

            return ResponseEntity.ok(
                    Map.of(
                            "success",
                            true,

                            "paymentId",
                            record.getPaymentId(),

                            "orderId",
                            recoveryOrderId,

                            "amount",
                            record.getAmount(),

                            "currency",
                            record.getCurrency(),

                            "action",
                            record.getRecoveryAction(),

                            "attempts",
                            record.getRecoveryAttempts(),

                            "maxAttempts",
                            recoveryPolicyService
                                    .getMaxRecoveryAttempts(),

                            "outcome",
                            "PENDING",

                            "message",
                            "Recovery payment order created"
                    )
            );


        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "❌ Recovery order creation failed"
            );

            System.out.println(
                    e.getMessage()
            );


            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "success",
                                    false,

                                    "message",
                                    "Unable to create recovery payment"
                            )
                    );
        }
    }
}