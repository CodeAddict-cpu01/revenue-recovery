package com.recovery.revenuerecovery.service;

import com.recovery.revenuerecovery.model.AIRecoveryDecision;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class AIRecoveryService {

    private static final String OLLAMA_URL =
            "http://localhost:11434/api/generate";

    private static final String MODEL =
            "llama3.2:3b";

    private final HttpClient httpClient;

    public AIRecoveryService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }


    /*
     * ==========================================
     * REAL RAZORPAY WEBHOOK ANALYSIS
     * ==========================================
     *
     * This method is used by the real Razorpay
     * payment.failed webhook flow.
     *
     */

    public AIRecoveryDecision analyzePaymentFailure(
            JSONObject webhook) {

        System.out.println();
        System.out.println("====================================");
        System.out.println("🧠 AI REVENUE RISK ANALYZER");
        System.out.println("====================================");

        try {

            // =====================================
            // 1. EXTRACT PAYMENT INFORMATION
            // =====================================

            JSONObject payload =
                    webhook.optJSONObject("payload");

            if (payload == null) {

                return fallbackDecision(
                        "Webhook payload is missing"
                );
            }

            JSONObject paymentContainer =
                    payload.optJSONObject("payment");

            if (paymentContainer == null) {

                return fallbackDecision(
                        "Payment information is missing"
                );
            }

            JSONObject payment =
                    paymentContainer.optJSONObject("entity");

            if (payment == null) {

                return fallbackDecision(
                        "Payment entity is missing"
                );
            }

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

            String errorCode =
                    payment.optString(
                            "error_code",
                            "unknown"
                    );

            String errorDescription =
                    payment.optString(
                            "error_description",
                            "Unknown failure"
                    );

            String method =
                    payment.optString(
                            "method",
                            "unknown"
                    );

            int amount =
                    payment.optInt(
                            "amount",
                            0
                    );


            // =====================================
            // 2. DISPLAY PAYMENT CONTEXT
            // =====================================

            System.out.println(
                    "Payment ID     : " +
                            paymentId
            );

            System.out.println(
                    "Order ID       : " +
                            orderId
            );

            System.out.println(
                    "Amount         : ₹" +
                            (amount / 100.0)
            );

            System.out.println(
                    "Payment Method : " +
                            method
            );

            System.out.println(
                    "Error Code     : " +
                            errorCode
            );

            System.out.println(
                    "Failure Reason : " +
                            errorDescription
            );


            // =====================================
            // 3. BUILD STRICT AI PROMPT
            // =====================================

            String prompt = """

                    You are an AI revenue recovery decision engine.

                    Analyze the failed Razorpay payment.

                    Return ONLY valid JSON.

                    You MUST use exactly these values.

                    failureType MUST be one of:

                    BANK_DECLINED
                    INSUFFICIENT_FUNDS
                    TIMEOUT
                    NETWORK_FAILURE
                    UPI_FAILURE
                    TECHNICAL_ERROR
                    UNKNOWN_FAILURE

                    recommendedAction MUST be one of:

                    ALTERNATE_PAYMENT_METHOD
                    RETRY_PAYMENT
                    SEND_PAYMENT_REMINDER
                    ESCALATE_TO_MERCHANT

                    priority MUST be exactly:

                    LOW
                    MEDIUM
                    HIGH

                    JSON format:

                    {
                      "failureType": "BANK_DECLINED",
                      "rootCause": "short explanation",
                      "recommendedAction": "ALTERNATE_PAYMENT_METHOD",
                      "recoveryProbability": 30,
                      "priority": "MEDIUM",
                      "reason": "short explanation"
                    }

                    RULES:

                    1. Use ONLY the supplied payment information.

                    2. Never invent customer information.

                    3. Never claim the payment was recovered.

                    4. recoveryProbability must be an integer from 0 to 100.

                    5. Do not recommend unlimited retries.

                    6. Bank-declined payments should normally use
                       ALTERNATE_PAYMENT_METHOD.

                    7. Timeout or network failures may use
                       RETRY_PAYMENT.

                    8. UPI failures may use
                       ALTERNATE_PAYMENT_METHOD or RETRY_PAYMENT.

                    9. Clearly permanent failures should not receive
                       unlimited retry recommendations.

                    10. If uncertain, use UNKNOWN_FAILURE,
                        SEND_PAYMENT_REMINDER and LOW priority.

                    PAYMENT DATA:

                    Payment ID:
                    %s

                    Order ID:
                    %s

                    Amount:
                    %d paise

                    Payment Method:
                    %s

                    Error Code:
                    %s

                    Error Description:
                    %s

                    Return ONLY the JSON object.
                    """.formatted(
                    paymentId,
                    orderId,
                    amount,
                    method,
                    errorCode,
                    errorDescription
            );


            // =====================================
            // 4. SEND TO LOCAL AI
            // =====================================

            System.out.println();
            System.out.println(
                    "🤖 SENDING PAYMENT CONTEXT TO LOCAL AI..."
            );

            System.out.println(
                    "AI Model: " +
                            MODEL
            );


            JSONObject requestBody =
                    new JSONObject();

            requestBody.put(
                    "model",
                    MODEL
            );

            requestBody.put(
                    "prompt",
                    prompt
            );

            requestBody.put(
                    "stream",
                    false
            );

            requestBody.put(
                    "format",
                    "json"
            );


            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            OLLAMA_URL
                                    )
                            )
                            .timeout(
                                    Duration.ofSeconds(60)
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(
                                                    requestBody.toString()
                                            )
                            )
                            .build();


            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString()
                    );


            // =====================================
            // 5. CHECK OLLAMA RESPONSE
            // =====================================

            if (response.statusCode() != 200) {

                throw new RuntimeException(
                        "Ollama returned HTTP " +
                                response.statusCode()
                );
            }


            JSONObject ollamaResponse =
                    new JSONObject(
                            response.body()
                    );


            String aiText =
                    ollamaResponse.optString(
                            "response",
                            ""
                    );


            if (aiText.isBlank()) {

                throw new RuntimeException(
                        "Ollama returned an empty response"
                );
            }


            System.out.println();
            System.out.println(
                    "🤖 LOCAL AI RESPONSE RECEIVED"
            );


            // =====================================
            // 6. PARSE AI JSON
            // =====================================

            JSONObject aiJson =
                    new JSONObject(
                            aiText.trim()
                    );


            String failureType =
                    aiJson.optString(
                            "failureType",
                            "UNKNOWN_FAILURE"
                    );


            String rootCause =
                    aiJson.optString(
                            "rootCause",
                            "Unknown failure cause."
                    );


            String recommendedAction =
                    aiJson.optString(
                            "recommendedAction",
                            "SEND_PAYMENT_REMINDER"
                    );


            int recoveryProbability =
                    aiJson.optInt(
                            "recoveryProbability",
                            30
                    );


            String priority =
                    aiJson.optString(
                            "priority",
                            "LOW"
                    );


            String reason =
                    aiJson.optString(
                            "reason",
                            "AI analysis completed."
                    );


            // =====================================
            // 7. NORMALIZE AI OUTPUT
            // =====================================

            failureType =
                    normalizeFailureType(
                            failureType
                    );


            recommendedAction =
                    normalizeAction(
                            recommendedAction
                    );


            priority =
                    normalizePriority(
                            priority
                    );


            recoveryProbability =
                    clampProbability(
                            recoveryProbability
                    );


            // =====================================
            // 8. SAFETY POLICY
            // =====================================

            /*
             * The AI recommends a strategy.
             *
             * The application still controls what
             * strategy is allowed to execute.
             */

            String lowerError =
                    (
                            errorCode + " " +
                                    errorDescription
                    ).toLowerCase();


            if (
                    lowerError.contains("declined") ||
                            lowerError.contains("bank decline") ||
                            lowerError.contains("bank_declined")
            ) {

                failureType =
                        "BANK_DECLINED";

                recommendedAction =
                        "ALTERNATE_PAYMENT_METHOD";

                if (recoveryProbability > 70) {

                    recoveryProbability = 70;
                }
            }


            if (
                    lowerError.contains("timeout") ||
                            lowerError.contains("timed out")
            ) {

                failureType =
                        "TIMEOUT";

                recommendedAction =
                        "RETRY_PAYMENT";
            }


            if (
                    lowerError.contains("network")
            ) {

                failureType =
                        "NETWORK_FAILURE";

                recommendedAction =
                        "RETRY_PAYMENT";
            }


            if (
                    method.equalsIgnoreCase("upi") &&
                            (
                                    lowerError.contains("upi") ||
                                            lowerError.contains("vpa")
                            )
            ) {

                failureType =
                        "UPI_FAILURE";

                if (
                        !recommendedAction.equals(
                                "RETRY_PAYMENT"
                        )
                ) {

                    recommendedAction =
                            "ALTERNATE_PAYMENT_METHOD";
                }
            }


            // =====================================
            // 9. CREATE FINAL DECISION
            // =====================================

            AIRecoveryDecision decision =
                    new AIRecoveryDecision(
                            failureType,
                            rootCause,
                            recommendedAction,
                            recoveryProbability,
                            priority,
                            reason
                    );


            // =====================================
            // 10. FINAL VALIDATION
            // =====================================

            validateDecision(
                    decision
            );


            // =====================================
            // 11. DISPLAY FINAL DECISION
            // =====================================

            System.out.println();
            System.out.println(
                    "===================================="
            );

            System.out.println(
                    "🤖 AI RECOVERY DECISION"
            );

            System.out.println(
                    "===================================="
            );

            System.out.println(
                    "Failure Type       : " +
                            decision.getFailureType()
            );

            System.out.println(
                    "Root Cause         : " +
                            decision.getRootCause()
            );

            System.out.println(
                    "Recovery Action    : " +
                            decision.getRecommendedAction()
            );

            System.out.println(
                    "Probability        : " +
                            decision.getRecoveryProbability() +
                            "%"
            );

            System.out.println(
                    "Priority           : " +
                            decision.getPriority()
            );

            System.out.println(
                    "Reason             : " +
                            decision.getReason()
            );

            System.out.println(
                    "===================================="
            );


            return decision;


        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "⚠️ AI ANALYSIS FAILED"
            );

            System.out.println(
                    "Reason: " +
                            e.getMessage()
            );

            System.out.println(
                    "Using safe fallback decision."
            );


            return fallbackDecision(
                    "Local AI analysis failed"
            );
        }
    }


    // ==========================================
    // BATCH / REUSABLE PAYMENT ANALYSIS
    // ==========================================
    //
    // This method allows the batch simulator
    // to use the exact same AI analysis pipeline
    // without needing a real Razorpay webhook.
    //
    // The existing webhook-based method above
    // remains unchanged.
    //

    public AIRecoveryDecision analyzePaymentFailure(
            String paymentId,
            String orderId,
            long amount,
            String method,
            String errorCode,
            String errorDescription) {

        JSONObject webhook =
                new JSONObject();

        JSONObject payload =
                new JSONObject();

        JSONObject paymentContainer =
                new JSONObject();

        JSONObject payment =
                new JSONObject();


        payment.put(
                "id",
                paymentId
        );

        payment.put(
                "order_id",
                orderId
        );

        payment.put(
                "amount",
                amount
        );

        payment.put(
                "method",
                method
        );

        payment.put(
                "error_code",
                errorCode
        );

        payment.put(
                "error_description",
                errorDescription
        );


        paymentContainer.put(
                "entity",
                payment
        );


        payload.put(
                "payment",
                paymentContainer
        );


        webhook.put(
                "payload",
                payload
        );


        return analyzePaymentFailure(
                webhook
        );
    }


    // ==========================================
    // NORMALIZE FAILURE TYPE
    // ==========================================

    private String normalizeFailureType(
            String failureType) {

        if (failureType == null) {

            return "UNKNOWN_FAILURE";
        }


        String value =
                failureType
                        .trim()
                        .toUpperCase()
                        .replace(" ", "_")
                        .replace("-", "_");


        switch (value) {

            case "BANKDECLINE":
            case "BANK_DECLINE":
            case "BANK_DECLINED":
            case "DECLINED":
            case "PAYMENT_DECLINED":

                return "BANK_DECLINED";


            case "INSUFFICIENT_FUNDS":
            case "INSUFFICIENT":
            case "INSUFFICIENT_BALANCE":

                return "INSUFFICIENT_FUNDS";


            case "TIMEOUT":
            case "TIMED_OUT":

                return "TIMEOUT";


            case "NETWORK":
            case "NETWORK_ERROR":
            case "NETWORK_FAILURE":

                return "NETWORK_FAILURE";


            case "UPI":
            case "UPI_ERROR":
            case "UPI_FAILURE":

                return "UPI_FAILURE";


            case "TECHNICAL":
            case "TECHNICAL_ERROR":
            case "SERVER_ERROR":

                return "TECHNICAL_ERROR";


            default:

                return "UNKNOWN_FAILURE";
        }
    }


    // ==========================================
    // NORMALIZE RECOVERY ACTION
    // ==========================================

    private String normalizeAction(
            String action) {

        if (action == null) {

            return "SEND_PAYMENT_REMINDER";
        }


        String value =
                action
                        .trim()
                        .toUpperCase()
                        .replace(" ", "_")
                        .replace("-", "_");


        if (
                value.contains("ALTERNATE") ||
                        value.contains("ANOTHER_PAYMENT") ||
                        value.contains("ANOTHER_METHOD")
        ) {

            return "ALTERNATE_PAYMENT_METHOD";
        }


        if (
                value.contains("RETRY")
        ) {

            return "RETRY_PAYMENT";
        }


        if (
                value.contains("ESCALATE")
        ) {

            return "ESCALATE_TO_MERCHANT";
        }


        if (
                value.contains("REMINDER") ||
                        value.contains("CONTACT")
        ) {

            return "SEND_PAYMENT_REMINDER";
        }


        return "SEND_PAYMENT_REMINDER";
    }


    // ==========================================
    // NORMALIZE PRIORITY
    // ==========================================

    private String normalizePriority(
            String priority) {

        if (priority == null) {

            return "LOW";
        }


        String value =
                priority
                        .trim()
                        .toUpperCase();


        if (value.equals("HIGH")) {

            return "HIGH";
        }


        if (value.equals("MEDIUM")) {

            return "MEDIUM";
        }


        return "LOW";
    }


    // ==========================================
    // CLAMP PROBABILITY
    // ==========================================

    private int clampProbability(
            int probability) {

        if (probability < 0) {

            return 0;
        }


        if (probability > 100) {

            return 100;
        }


        return probability;
    }


    // ==========================================
    // VALIDATE FINAL DECISION
    // ==========================================

    private void validateDecision(
            AIRecoveryDecision decision) {

        if (decision == null) {

            throw new IllegalArgumentException(
                    "AI decision is null"
            );
        }


        int probability =
                decision.getRecoveryProbability();


        if (
                probability < 0 ||
                        probability > 100
        ) {

            throw new IllegalArgumentException(
                    "Invalid recovery probability"
            );
        }


        String priority =
                decision.getPriority();


        if (
                !priority.equals("LOW") &&
                        !priority.equals("MEDIUM") &&
                        !priority.equals("HIGH")
        ) {

            throw new IllegalArgumentException(
                    "Invalid priority"
            );
        }


        String failureType =
                decision.getFailureType();


        if (
                !failureType.equals("BANK_DECLINED") &&
                        !failureType.equals("INSUFFICIENT_FUNDS") &&
                        !failureType.equals("TIMEOUT") &&
                        !failureType.equals("NETWORK_FAILURE") &&
                        !failureType.equals("UPI_FAILURE") &&
                        !failureType.equals("TECHNICAL_ERROR") &&
                        !failureType.equals("UNKNOWN_FAILURE")
        ) {

            throw new IllegalArgumentException(
                    "Invalid failure type"
            );
        }


        String action =
                decision.getRecommendedAction();


        if (
                !action.equals(
                        "ALTERNATE_PAYMENT_METHOD"
                ) &&
                        !action.equals(
                                "RETRY_PAYMENT"
                        ) &&
                        !action.equals(
                                "SEND_PAYMENT_REMINDER"
                        ) &&
                        !action.equals(
                                "ESCALATE_TO_MERCHANT"
                        )
        ) {

            throw new IllegalArgumentException(
                    "Invalid recovery action"
            );
        }
    }


    // ==========================================
    // SAFE FALLBACK
    // ==========================================

    private AIRecoveryDecision fallbackDecision(
            String reason) {

        return new AIRecoveryDecision(

                "UNKNOWN_FAILURE",

                "Unable to confidently determine " +
                        "the payment failure cause.",

                "SEND_PAYMENT_REMINDER",

                30,

                "LOW",

                reason +
                        ". A conservative recovery " +
                        "action was selected."
        );
    }
}