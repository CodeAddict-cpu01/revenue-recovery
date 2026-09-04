package com.recovery.revenuerecovery.model;

public class AIRecoveryDecision {

    public String failureType;

    public String rootCause;

    public String recommendedAction;

    public int recoveryProbability;

    public String priority;

    public String reason;

    public AIRecoveryDecision() {
    }

    public AIRecoveryDecision(
            String failureType,
            String rootCause,
            String recommendedAction,
            int recoveryProbability,
            String priority,
            String reason) {

        this.failureType = failureType;
        this.rootCause = rootCause;
        this.recommendedAction = recommendedAction;
        this.recoveryProbability = recoveryProbability;
        this.priority = priority;
        this.reason = reason;
    }

    public String getFailureType() {
        return failureType;
    }

    public void setFailureType(String failureType) {
        this.failureType = failureType;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public int getRecoveryProbability() {
        return recoveryProbability;
    }

    public void setRecoveryProbability(int recoveryProbability) {
        this.recoveryProbability = recoveryProbability;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {

        return "\n" +
                "Failure Type          : " + failureType + "\n" +
                "Root Cause            : " + rootCause + "\n" +
                "Recommended Action    : " + recommendedAction + "\n" +
                "Recovery Probability  : " + recoveryProbability + "%\n" +
                "Priority              : " + priority + "\n" +
                "Reason                : " + reason;
    }
}