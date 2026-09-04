package com.recovery.revenuerecovery.model;

public class RecoveryDecision {

    private String failureType;
    private String recoveryAction;
    private int recoveryProbability;
    private String priority;
    private String reason;

    public RecoveryDecision(
            String failureType,
            String recoveryAction,
            int recoveryProbability,
            String priority,
            String reason) {

        this.failureType = failureType;
        this.recoveryAction = recoveryAction;
        this.recoveryProbability = recoveryProbability;
        this.priority = priority;
        this.reason = reason;
    }

    public String getFailureType() {
        return failureType;
    }

    public String getRecoveryAction() {
        return recoveryAction;
    }

    public int getRecoveryProbability() {
        return recoveryProbability;
    }

    public String getPriority() {
        return priority;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "\n" +
                "Failure Type       : " + failureType + "\n" +
                "Recovery Action    : " + recoveryAction + "\n" +
                "Recovery Probability: " + recoveryProbability + "%\n" +
                "Priority            : " + priority + "\n" +
                "Reason              : " + reason;
    }
}