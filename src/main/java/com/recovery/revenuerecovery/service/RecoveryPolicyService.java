package com.recovery.revenuerecovery.service;

import com.recovery.revenuerecovery.model.PaymentRecord;
import org.springframework.stereotype.Service;

@Service
public class RecoveryPolicyService {

    /*
     * Maximum number of recovery attempts
     * allowed for a payment.
     *
     * This is intentionally bounded to prevent
     * infinite retries or uncontrolled payment actions.
     */
    private static final int MAX_RECOVERY_ATTEMPTS = 1;


    /*
     * Determines whether recovery is allowed.
     */
    public boolean canRecover(PaymentRecord record) {

        if (record == null) {
            return false;
        }

        /*
         * Never recover an already recovered payment.
         */
        if ("RECOVERED".equalsIgnoreCase(
                record.getStatus())) {

            return false;
        }


        /*
         * Never exceed the maximum number
         * of recovery attempts.
         */
        if (record.getRecoveryAttempts()
                >= MAX_RECOVERY_ATTEMPTS) {

            return false;
        }


        /*
         * If the recovery outcome has explicitly
         * been stopped, do not continue.
         */
        if ("STOPPED".equalsIgnoreCase(
                record.getRecoveryOutcome())) {

            return false;
        }


        /*
         * If the recovery action is missing,
         * do not execute an undefined action.
         */
        if (record.getRecoveryAction() == null
                || record.getRecoveryAction().isBlank()) {

            return false;
        }


        return isAllowedAction(
                record.getRecoveryAction()
        );
    }


    /*
     * Defines the bounded actions that the system
     * is currently allowed to execute.
     */
    private boolean isAllowedAction(
            String action) {

        return switch (
                action.toUpperCase()) {

            case "RETRY_PAYMENT",
                 "ALTERNATE_PAYMENT_METHOD",
                 "SEND_PAYMENT_REMINDER" ->
                    true;

            default ->
                    false;
        };
    }


    /*
     * Called when a recovery attempt is about
     * to be executed.
     */
    public void registerRecoveryAttempt(
            PaymentRecord record) {

        int attempts =
                record.getRecoveryAttempts();

        record.setRecoveryAttempts(
                attempts + 1
        );

        record.setRecoveryOutcome(
                "PENDING"
        );
    }


    /*
     * Called when the policy engine decides
     * that no further recovery should happen.
     */
    public void stopRecovery(
            PaymentRecord record,
            String reason) {

        record.setStatus(
                "FAILED"
        );

        record.setRecoveryOutcome(
                "STOPPED"
        );

        String existingReason =
                record.getReason();

        if (existingReason == null) {
            existingReason = "";
        }

        record.setReason(
                existingReason +
                        " | Recovery stopped: " +
                        reason
        );
    }


    public int getMaxRecoveryAttempts() {

        return MAX_RECOVERY_ATTEMPTS;
    }
}