package com.coffee.cfdemo.model;

import java.math.BigDecimal;

public class Payment {

    private String paymentId;
    private BigDecimal amount;
    private String status;
    private boolean available;

    public Payment() {
    }

    public Payment(String paymentId, BigDecimal amount, String status, boolean available) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.status = status;
        this.available = available;
    }

    public static Payment unavailable() {
        return new Payment(null, BigDecimal.ZERO, "UNAVAILABLE", false);
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
