package com.coffee.cfdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point.
 *
 * This demo simulates a common interview scenario:
 * "A microservice is slow because of external API calls — how do you optimize it?"
 *
 * The OrderService in this project calls 3 *independent* simulated external
 * services (User, Payment, Shipping) sequentially first (slow), then shows
 * the CompletableFuture-based fix (parallel, with timeout + fallback).
 */
@SpringBootApplication
public class CompletableFutureDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompletableFutureDemoApplication.class, args);
    }
}
