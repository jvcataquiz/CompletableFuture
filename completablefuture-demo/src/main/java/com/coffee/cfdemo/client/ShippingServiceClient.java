package com.coffee.cfdemo.client;

import com.coffee.cfdemo.model.Shipping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShippingServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ShippingServiceClient.class);
    private static final int SIMULATED_LATENCY_MS = 15000;

    public Shipping getShipping(String orderId) {
        log.info("Calling Shipping Service for orderId={} ...", orderId);
        sleep(SIMULATED_LATENCY_MS);
        log.info("Shipping Service responded for orderId={}", orderId);
        return new Shipping("ship-" + orderId, "J&T Express", "2026-07-02", true);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Shipping service call interrupted", e);
        }
    }
}
