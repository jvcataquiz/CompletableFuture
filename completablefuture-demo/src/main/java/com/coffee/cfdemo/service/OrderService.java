package com.coffee.cfdemo.service;

import com.coffee.cfdemo.client.PaymentServiceClient;
import com.coffee.cfdemo.client.ShippingServiceClient;
import com.coffee.cfdemo.client.UserServiceClient;
import com.coffee.cfdemo.dto.OrderDetails;
import com.coffee.cfdemo.model.Payment;
import com.coffee.cfdemo.model.Shipping;
import com.coffee.cfdemo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates the exact optimization discussed for the interview question:
 * "A microservice is slow because of external API calls — how do you optimize it?"
 *
 * Two methods, same 3 external calls, two strategies:
 *
 *   getOrderDetailsSequential() - BAD: calls run one after another.
 *       Total time = sum of all 3 calls (~750ms), even though the calls
 *       are completely independent of each other.
 *
 *   getOrderDetailsParallel()   - GOOD: calls run concurrently via
 *       CompletableFuture, each with its own timeout and fallback.
 *       Total time = the SLOWEST single call (~300ms), not the sum.
 *
 * Hit both endpoints and compare totalTimeMs in the response to see the
 * difference directly, not just in theory.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final UserServiceClient userServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final ShippingServiceClient shippingServiceClient;
    private final Executor externalApiExecutor;

    public OrderService(
            UserServiceClient userServiceClient,
            PaymentServiceClient paymentServiceClient,
            ShippingServiceClient shippingServiceClient,
            @Qualifier("externalApiExecutor") Executor externalApiExecutor) {
        this.userServiceClient = userServiceClient;
        this.paymentServiceClient = paymentServiceClient;
        this.shippingServiceClient = shippingServiceClient;
        this.externalApiExecutor = externalApiExecutor;
    }


    public OrderDetails getOrderDetailsSequential(String orderId) {
        long start = System.currentTimeMillis();

        User user = userServiceClient.getUser(orderId);
        Payment payment = paymentServiceClient.getPayment(orderId);
        Shipping shipping = shippingServiceClient.getShipping(orderId);

        long totalTime = System.currentTimeMillis() - start;
        log.info("SEQUENTIAL total time: {}ms", totalTime);

        return new OrderDetails(orderId, user, payment, shipping, totalTime, "SEQUENTIAL");
    }

   
    public OrderDetails getOrderDetailsParallel(String orderId) {
        long start = System.currentTimeMillis();

        CompletableFuture<User> userFuture = CompletableFuture
                .supplyAsync(() -> userServiceClient.getUser(orderId), externalApiExecutor)
                .orTimeout(2, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.warn("User service call failed or timed out: {}", ex.getMessage());
                    return User.unavailable();
                });

        CompletableFuture<Payment> paymentFuture = CompletableFuture
                .supplyAsync(() -> paymentServiceClient.getPayment(orderId), externalApiExecutor)
                .orTimeout(2, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.warn("Payment service call failed or timed out: {}", ex.getMessage());
                    return Payment.unavailable();
                });

        CompletableFuture<Shipping> shippingFuture = CompletableFuture
                .supplyAsync(() -> shippingServiceClient.getShipping(orderId), externalApiExecutor)
                .orTimeout(2, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.warn("Shipping service call failed or timed out: {}", ex.getMessage());
                    return Shipping.unavailable();
                });

        OrderDetails result = CompletableFuture
                .allOf(userFuture, paymentFuture, shippingFuture)
                .thenApply(v -> {
                    long totalTime = System.currentTimeMillis() - start;
                    log.info("PARALLEL total time: {}ms", totalTime);
                    return new OrderDetails(
                            orderId,
                            userFuture.join(),
                            paymentFuture.join(),
                            shippingFuture.join(),
                            totalTime,
                            "PARALLEL"
                    );
                })
                .join();

        return result;
    }
}
