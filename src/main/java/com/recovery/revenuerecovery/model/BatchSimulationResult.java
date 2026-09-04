package com.recovery.revenuerecovery.model;

public class BatchSimulationResult {

    private int totalPayments;

    private int recoveredPayments;

    private int stoppedPayments;

    private int failedPayments;

    private long totalRevenueAtRisk;

    private long expectedRecovery;

    private long simulatedRecoveredRevenue;

    private double recoveryRate;

    private double expectedRecoveryRate;


    public BatchSimulationResult() {
    }


    public int getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(int totalPayments) {
        this.totalPayments = totalPayments;
    }


    public int getRecoveredPayments() {
        return recoveredPayments;
    }

    public void setRecoveredPayments(int recoveredPayments) {
        this.recoveredPayments = recoveredPayments;
    }


    public int getStoppedPayments() {
        return stoppedPayments;
    }

    public void setStoppedPayments(int stoppedPayments) {
        this.stoppedPayments = stoppedPayments;
    }


    public int getFailedPayments() {
        return failedPayments;
    }

    public void setFailedPayments(int failedPayments) {
        this.failedPayments = failedPayments;
    }


    public long getTotalRevenueAtRisk() {
        return totalRevenueAtRisk;
    }

    public void setTotalRevenueAtRisk(
            long totalRevenueAtRisk) {

        this.totalRevenueAtRisk =
                totalRevenueAtRisk;
    }


    public long getExpectedRecovery() {
        return expectedRecovery;
    }

    public void setExpectedRecovery(
            long expectedRecovery) {

        this.expectedRecovery =
                expectedRecovery;
    }


    public long getSimulatedRecoveredRevenue() {
        return simulatedRecoveredRevenue;
    }

    public void setSimulatedRecoveredRevenue(
            long simulatedRecoveredRevenue) {

        this.simulatedRecoveredRevenue =
                simulatedRecoveredRevenue;
    }


    public double getRecoveryRate() {
        return recoveryRate;
    }

    public void setRecoveryRate(
            double recoveryRate) {

        this.recoveryRate =
                recoveryRate;
    }


    public double getExpectedRecoveryRate() {
        return expectedRecoveryRate;
    }

    public void setExpectedRecoveryRate(
            double expectedRecoveryRate) {

        this.expectedRecoveryRate =
                expectedRecoveryRate;
    }
}