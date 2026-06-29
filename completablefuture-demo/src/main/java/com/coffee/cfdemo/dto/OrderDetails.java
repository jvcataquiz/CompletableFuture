package com.coffee.cfdemo.dto;

import com.coffee.cfdemo.model.Payment;
import com.coffee.cfdemo.model.Shipping;
import com.coffee.cfdemo.model.User;

/**
 * Aggregate response combining results from 3 independent external calls.
 * Includes totalTimeMs so you can SEE the sequential-vs-parallel difference
 * directly in the JSON response, not just in logs.
 */
public class OrderDetails {

    private String orderId;
    private User user;
    private Payment payment;
    private Shipping shipping;
    private long totalTimeMs;
    private String strategy; // "SEQUENTIAL" or "PARALLEL"

    public OrderDetails() {
    }

    public OrderDetails(String orderId, User user, Payment payment, Shipping shipping,
                         long totalTimeMs, String strategy) {
        this.orderId = orderId;
        this.user = user;
        this.payment = payment;
        this.shipping = shipping;
        this.totalTimeMs = totalTimeMs;
        this.strategy = strategy;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Shipping getShipping() {
        return shipping;
    }

    public void setShipping(Shipping shipping) {
        this.shipping = shipping;
    }

    public long getTotalTimeMs() {
        return totalTimeMs;
    }

    public void setTotalTimeMs(long totalTimeMs) {
        this.totalTimeMs = totalTimeMs;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}
