package com.coffee.cfdemo.client;

import com.coffee.cfdemo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);
    private static final int SIMULATED_LATENCY_MS = 15000;

    public User getUser(String orderId) {
        log.info("Calling User Service for orderId={} ...", orderId);
        sleep(SIMULATED_LATENCY_MS);
        log.info("User Service responded for orderId={}", orderId);
        return new User("user-" + orderId, "Juan Dela Cruz", true);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("User service call interrupted", e);
        }
    }
}
