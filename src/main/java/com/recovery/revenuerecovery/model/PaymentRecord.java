package com.recovery.revenuerecovery.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_records")
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentId;

    private String orderId;

    private long amount;

    private String currency;

    private String paymentMethod;

    private String status;

    private String failureType;

    private String recoveryAction;

    private int recoveryProbability;

    private String priority;

    @Column(length = 1000)
    private String reason;

    private LocalDateTime createdAt;

    private String recoveryOrderId;

    private String recoveryPaymentId;

    /*
     * Recovery control fields
     *
     * recoveryAttempts:
     * 0 = no recovery attempt started
     * 1 = first recovery attempt
     *
     * The policy engine can use this value
     * to enforce bounded recovery.
     */
    @Column(nullable = false, columnDefinition = "integer default 0")
private int recoveryAttempts;

    /*
     * Possible values:
     *
     * PENDING
     * RECOVERED
     * STOPPED
     * FAILED
     */
    private String recoveryOutcome;

    public PaymentRecord() {
    }

    public Long getId() {
        return id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailureType() {
        return failureType;
    }

    public void setFailureType(String failureType) {
        this.failureType = failureType;
    }

    public String getRecoveryAction() {
        return recoveryAction;
    }

    public void setRecoveryAction(String recoveryAction) {
        this.recoveryAction = recoveryAction;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRecoveryOrderId() {
        return recoveryOrderId;
    }

    public void setRecoveryOrderId(String recoveryOrderId) {
        this.recoveryOrderId = recoveryOrderId;
    }

    public String getRecoveryPaymentId() {
        return recoveryPaymentId;
    }

    public void setRecoveryPaymentId(String recoveryPaymentId) {
        this.recoveryPaymentId = recoveryPaymentId;
    }

    public int getRecoveryAttempts() {
        return recoveryAttempts;
    }

    public void setRecoveryAttempts(int recoveryAttempts) {
        this.recoveryAttempts = recoveryAttempts;
    }

    public String getRecoveryOutcome() {
        return recoveryOutcome;
    }

    public void setRecoveryOutcome(String recoveryOutcome) {
        this.recoveryOutcome = recoveryOutcome;
    }
}