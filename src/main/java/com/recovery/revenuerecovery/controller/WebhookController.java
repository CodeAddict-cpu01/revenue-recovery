package com.recovery.revenuerecovery.controller;

import com.recovery.revenuerecovery.model.AIRecoveryDecision;
import com.recovery.revenuerecovery.model.RecoveryDecision;
import com.recovery.revenuerecovery.repository.PaymentRecordRepository;
import com.recovery.revenuerecovery.service.AIRecoveryService;
import com.recovery.revenuerecovery.service.RecoveryService;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    private final AIRecoveryService aiRecoveryService;

    private final RecoveryService recoveryService;

    private final PaymentRecordRepository paymentRecordRepository;

    public WebhookController(
            AIRecoveryService aiRecoveryService,
            RecoveryService recoveryService,
            PaymentRecordRepository paymentRecordRepository) {

        this.aiRecoveryService = aiRecoveryService;
        this.recoveryService = recoveryService;
        this.paymentRecordRepository = paymentRecordRepository;
    }


    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(

            @RequestBody String payload,

            @RequestHeader(
                    value = "X-Razorpay-Signature",
                    required = false
            )
            String signature,

            @RequestHeader(
                    value = "x-razorpay-event-id",
                    required = false
            )
            String eventId) {


        try {

            /*
             * =====================================
             * 1. CHECK SIGNATURE
             * =====================================
             */

            if (
                    signature == null ||
                    signature.isBlank()
            ) {

                System.out.println(
                        "❌ Missing Razorpay signature"
                );

                return ResponseEntity
                        .badRequest()
                        .body("Missing signature");
            }


            /*
             * =====================================
             * 2. VERIFY WEBHOOK
             * =====================================
             */

            Utils.verifyWebhookSignature(
                    payload,
                    signature,
                    webhookSecret
            );


            JSONObject webhook =
                    new JSONObject(payload);

            String event =
                    webhook.optString(
                            "event",
                            "unknown"
                    );


            /*
             * =====================================
             * 3. WEBHOOK RECEIVED
             * =====================================
             */

            System.out.println();
            System.out.println(
                    "===================================="
            );

            System.out.println(
                    "✅ RAZORPAY WEBHOOK RECEIVED"
            );

            System.out.println(
                    "Event ID: " + eventId
            );

            System.out.println(
                    "Event: " + event
            );

            System.out.println(
                    "===================================="
            );


            /*
             * =====================================
             * 4. PAYMENT FAILED
             * =====================================
             */

            if (
                    "payment.failed".equals(event)
            ) {

                System.out.println(
                        "🔴 PAYMENT FAILED"
                );


                /*
                 * =================================
                 * Extract payment ID
                 * =================================
                 */

                String paymentId =
                        extractPaymentId(
                                webhook
                        );


                /*
                 * =================================
                 * Prevent duplicate processing
                 * =================================
                 */

                if (
                        paymentId != null &&
                        paymentRecordRepository
                                .findByPaymentId(paymentId)
                                .isPresent()
                ) {

                    System.out.println();
                    System.out.println(
                            "⚠️ DUPLICATE WEBHOOK"
                    );

                    System.out.println(
                            "Payment already exists: " +
                                    paymentId
                    );

                    System.out.println(
                            "Skipping AI analysis."
                    );

                    return ResponseEntity.ok(
                            "Webhook already processed"
                    );
                }


                /*
                 * =================================
                 * AI ANALYSIS
                 * =================================
                 */

                AIRecoveryDecision aiDecision =
                        aiRecoveryService
                                .analyzePaymentFailure(
                                        webhook
                                );


                /*
                 * =================================
                 * SAVE PAYMENT
                 * =================================
                 *
                 * RecoveryService currently uses
                 * its own rule-based decision.
                 *
                 * We will update it next so that
                 * the AI decision is what gets saved.
                 */

               RecoveryDecision savedDecision =
        recoveryService
                .recoverPayment(
                        webhook,
                        aiDecision
                );


                /*
                 * =================================
                 * DISPLAY AI DECISION
                 * =================================
                 */

                System.out.println();
                System.out.println(
                        "🧠 AI RECOVERY RECOMMENDATION"
                );

                System.out.println(
                        "Failure Type       : " +
                                aiDecision
                                        .getFailureType()
                );

                System.out.println(
                        "Root Cause         : " +
                                aiDecision
                                        .getRootCause()
                );

                System.out.println(
                        "Recovery Action    : " +
                                aiDecision
                                        .getRecommendedAction()
                );

                System.out.println(
                        "Probability        : " +
                                aiDecision
                                        .getRecoveryProbability() +
                                "%"
                );

                System.out.println(
                        "Priority           : " +
                                aiDecision
                                        .getPriority()
                );

                System.out.println(
                        "Reason             : " +
                                aiDecision
                                        .getReason()
                );

                System.out.println(
                        "===================================="
                );

            }


            /*
             * =====================================
             * 5. PAYMENT CAPTURED
             * =====================================
             */

            else if (
                    "payment.captured".equals(event)
            ) {

                System.out.println(
                        "🟢 PAYMENT CAPTURED"
                );

                System.out.println(
                        "Payment successfully captured."
                );

            }


            /*
             * =====================================
             * 6. ORDER PAID
             * =====================================
             */

            else if (
                    "order.paid".equals(event)
            ) {

                System.out.println(
                        "🟢 ORDER PAID"
                );

                System.out.println(
                        "Order successfully paid."
                );

            }


            /*
             * =====================================
             * 7. OTHER EVENTS
             * =====================================
             */

            else {

                System.out.println(
                        "ℹ️ Event ignored: " +
                                event
                );

            }


            System.out.println(
                    "===================================="
            );

            System.out.println();


            return ResponseEntity.ok(
                    "Webhook received"
            );


        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "❌ WEBHOOK PROCESSING FAILED"
            );

            System.out.println(
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Webhook processing failed"
                    );
        }
    }


    /*
     * ==========================================
     * EXTRACT PAYMENT ID
     * ==========================================
     */

    private String extractPaymentId(
            JSONObject webhook) {

        try {

            JSONObject payload =
                    webhook.optJSONObject(
                            "payload"
                    );

            if (payload == null) {
                return null;
            }

            JSONObject paymentContainer =
                    payload.optJSONObject(
                            "payment"
                    );

            if (paymentContainer == null) {
                return null;
            }

            JSONObject payment =
                    paymentContainer.optJSONObject(
                            "entity"
                    );

            if (payment == null) {
                return null;
            }

            return payment.optString(
                    "id",
                    null
            );

        } catch (Exception e) {

            return null;
        }
    }
}