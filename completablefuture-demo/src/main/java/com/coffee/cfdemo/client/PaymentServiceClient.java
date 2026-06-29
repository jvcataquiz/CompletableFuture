package com.coffee.cfdemo.client;

import com.coffee.cfdemo.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentServiceClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceClient.class);
    private static final int SIMULATED_LATENCY_MS = 15000;

    public Payment getPayment(String orderId) {
        log.info("Calling Payment Service for orderId={} ...", orderId);
        sleep(SIMULATED_LATENCY_MS);
        log.info("Payment Service responded for orderId={}", orderId);
        return new Payment("pay-" + orderId, new BigDecimal("1499.00"), "CONFIRMED", true);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Payment service call interrupted", e);
        }
    }
}
