package com.recovery.revenuerecovery.service;

import com.recovery.revenuerecovery.model.AIRecoveryDecision;
import com.recovery.revenuerecovery.model.BatchSimulationResult;
import com.recovery.revenuerecovery.model.PaymentRecord;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class BatchSimulationService {

    private final AIRecoveryService aiRecoveryService;
    private final RecoveryPolicyService recoveryPolicyService;

    /*
     * Fixed seed makes the simulation reproducible.
     * This is useful during the Buildathon demo because
     * the same batch produces comparable results.
     */
    private final Random random = new Random(42);

    private static final int MAX_BATCH_SIZE = 100;

    public BatchSimulationService(
            AIRecoveryService aiRecoveryService,
            RecoveryPolicyService recoveryPolicyService) {

        this.aiRecoveryService = aiRecoveryService;
        this.recoveryPolicyService = recoveryPolicyService;
    }


    public BatchSimulationResult runSimulation(int count) {

        if (count < 1) {
            throw new IllegalArgumentException(
                    "Simulation count must be at least 1"
            );
        }

        if (count > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException(
                    "Simulation count cannot exceed " +
                            MAX_BATCH_SIZE
            );
        }


        BatchSimulationResult result =
                new BatchSimulationResult();

        long totalRevenueAtRisk = 0;
        long expectedRecovery = 0;
        long simulatedRecoveredRevenue = 0;

        int recoveredPayments = 0;
        int stoppedPayments = 0;
        int failedPayments = 0;


        System.out.println();
        System.out.println("====================================");
        System.out.println("🤖 BATCH RECOVERY SIMULATION");
        System.out.println("====================================");
        System.out.println("Payments to simulate: " + count);
        System.out.println("Maximum allowed     : " + MAX_BATCH_SIZE);
        System.out.println();


        for (int i = 1; i <= count; i++) {

            long amount = generateAmount(i);

            String paymentId =
                    "sim_pay_" + String.format("%03d", i);

            String orderId =
                    "sim_order_" + String.format("%03d", i);

            SimulationScenario scenario =
                    generateScenario(i);


            System.out.println(
                    "Simulation " +
                            i +
                            "/" +
                            count +
                            " | " +
                            scenario.errorCode +
                            " | ₹" +
                            (amount / 100.0)
            );


            /*
             * Run the same AI decision engine used by
             * real failed payments.
             */
            AIRecoveryDecision decision =
                    aiRecoveryService.analyzePaymentFailure(
                            paymentId,
                            orderId,
                            amount,
                            scenario.method,
                            scenario.errorCode,
                            scenario.errorDescription
                    );


            totalRevenueAtRisk += amount;


            int probability =
                    clampProbability(
                            decision.getRecoveryProbability()
                    );


            expectedRecovery +=
                    (amount * probability) / 100;


            /*
             * Create an in-memory PaymentRecord.
             *
             * IMPORTANT:
             * This record is NOT saved to the database.
             * Simulation must not pollute real payment data.
             */
            PaymentRecord record =
                    new PaymentRecord();

            record.setPaymentId(paymentId);
            record.setOrderId(orderId);
            record.setAmount(amount);
            record.setCurrency("INR");
            record.setPaymentMethod(scenario.method);
            record.setStatus("FAILED");
            record.setFailureType(
                    decision.getFailureType()
            );
            record.setRecoveryAction(
                    decision.getRecommendedAction()
            );
            record.setRecoveryProbability(
                    probability
            );
            record.setPriority(
                    decision.getPriority()
            );
            record.setReason(
                    decision.getReason()
            );
            record.setRecoveryAttempts(0);


            /*
             * Apply the SAME policy engine used
             * by real recovery.
             */
            boolean policyApproved =
                    recoveryPolicyService.canRecover(record);


            if (!policyApproved) {

                stoppedPayments++;

                record.setRecoveryOutcome("STOPPED");

                System.out.println(
                        "   🛑 POLICY STOPPED"
                );

                System.out.println(
                        "   Action: " +
                                decision.getRecommendedAction()
                );

                continue;
            }


            /*
             * Policy approved.
             *
             * Now simulate whether the recovery actually
             * succeeds using the AI probability.
             */
            record.setRecoveryAttempts(1);
            record.setRecoveryOutcome("PENDING");


            boolean recovered =
                    random.nextInt(100) < probability;


            if (recovered) {

                recoveredPayments++;

                simulatedRecoveredRevenue += amount;

                record.setStatus("RECOVERED");
                record.setRecoveryOutcome("RECOVERED");

                System.out.println(
                        "   ✅ SIMULATED RECOVERY"
                );

            } else {

                failedPayments++;

                record.setRecoveryOutcome("FAILED");

                System.out.println(
                        "   ❌ SIMULATED FAILURE"
                );
            }


            System.out.println(
                    "   AI Action: " +
                            decision.getRecommendedAction()
            );

            System.out.println(
                    "   Probability: " +
                            probability +
                            "%"
            );

            System.out.println();
        }


        double recoveryRate =
                count == 0
                        ? 0
                        : (recoveredPayments * 100.0) / count;


        double expectedRecoveryRate =
                totalRevenueAtRisk == 0
                        ? 0
                        : (expectedRecovery * 100.0)
                        / totalRevenueAtRisk;


        result.setTotalPayments(count);

        result.setRecoveredPayments(
                recoveredPayments
        );

        result.setStoppedPayments(
                stoppedPayments
        );

        result.setFailedPayments(
                failedPayments
        );

        result.setTotalRevenueAtRisk(
                totalRevenueAtRisk
        );

        result.setExpectedRecovery(
                expectedRecovery
        );

        result.setSimulatedRecoveredRevenue(
                simulatedRecoveredRevenue
        );

        result.setRecoveryRate(
                round(recoveryRate)
        );

        result.setExpectedRecoveryRate(
                round(expectedRecoveryRate)
        );


        System.out.println("====================================");
        System.out.println("📊 SIMULATION RESULT");
        System.out.println("====================================");

        System.out.println(
                "Payments Simulated : " +
                        count
        );

        System.out.println(
                "Revenue At Risk    : ₹" +
                        (totalRevenueAtRisk / 100.0)
        );

        System.out.println(
                "Expected Recovery  : ₹" +
                        (expectedRecovery / 100.0)
        );

        System.out.println(
                "Simulated Recovered: ₹" +
                        (simulatedRecoveredRevenue / 100.0)
        );

        System.out.println(
                "Recovered Payments : " +
                        recoveredPayments
        );

        System.out.println(
                "Stopped Payments   : " +
                        stoppedPayments
        );

        System.out.println(
                "Failed Recoveries  : " +
                        failedPayments
        );

        System.out.println(
                "Recovery Rate      : " +
                        round(recoveryRate) +
                        "%"
        );

        System.out.println(
                "Expected Rate      : " +
                        round(expectedRecoveryRate) +
                        "%"
        );

        System.out.println("====================================");


        return result;
    }


    /*
     * Generate realistic payment amounts.
     *
     * Values are in paise.
     */
    private long generateAmount(int index) {

        int[] amounts = {
                50000,    // ₹500
                75000,    // ₹750
                100000,   // ₹1,000
                150000,   // ₹1,500
                200000,   // ₹2,000
                250000,   // ₹2,500
                500000,   // ₹5,000
                750000,   // ₹7,500
                1000000,  // ₹10,000
                1500000   // ₹15,000
        };

        return amounts[(index - 1) % amounts.length];
    }


    /*
     * Generate different failure scenarios so the AI
     * has different contexts to analyze.
     */
    private SimulationScenario generateScenario(int index) {

        switch (index % 8) {

            case 0:
                return new SimulationScenario(
                        "card",
                        "BAD_REQUEST_ERROR",
                        "Payment declined by the bank"
                );

            case 1:
                return new SimulationScenario(
                        "card",
                        "INSUFFICIENT_FUNDS",
                        "Customer account has insufficient funds"
                );

            case 2:
                return new SimulationScenario(
                        "card",
                        "GATEWAY_TIMEOUT",
                        "Payment gateway request timed out"
                );

            case 3:
                return new SimulationScenario(
                        "upi",
                        "BAD_REQUEST_ERROR",
                        "UPI payment failed at the bank"
                );

            case 4:
                return new SimulationScenario(
                        "upi",
                        "GATEWAY_ERROR",
                        "UPI network failure during payment"
                );

            case 5:
                return new SimulationScenario(
                        "netbanking",
                        "BAD_REQUEST_ERROR",
                        "Bank declined the transaction"
                );

            case 6:
                return new SimulationScenario(
                        "card",
                        "SERVER_ERROR",
                        "Temporary technical error during payment"
                );

            default:
                return new SimulationScenario(
                        "wallet",
                        "UNKNOWN_ERROR",
                        "Unknown payment processing failure"
                );
        }
    }


    private int clampProbability(int probability) {

        if (probability < 0) {
            return 0;
        }

        if (probability > 100) {
            return 100;
        }

        return probability;
    }


    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }


    /*
     * Internal representation of a synthetic
     * failed-payment scenario.
     */
    private static class SimulationScenario {

        private final String method;
        private final String errorCode;
        private final String errorDescription;


        private SimulationScenario(
                String method,
                String errorCode,
                String errorDescription) {

            this.method = method;
            this.errorCode = errorCode;
            this.errorDescription = errorDescription;
        }
    }
}