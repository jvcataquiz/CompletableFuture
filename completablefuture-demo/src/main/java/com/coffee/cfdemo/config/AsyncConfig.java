package com.coffee.cfdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Dedicated thread pool for outbound calls to external services.
 *
 * WHY a dedicated pool instead of letting CompletableFuture.supplyAsync()
 * fall back to ForkJoinPool.commonPool():
 *
 *   1. commonPool() is shared across the entire JVM. If some unrelated
 *      CPU-bound code (or another part of this app) also uses it, your
 *      external API calls compete with that for threads — a slow/hanging
 *      external API can then starve threads needed elsewhere.
 *
 *   2. A dedicated pool isolates the blast radius. This is the same idea
 *      as the "bulkhead" resilience pattern: if the external API is having
 *      a bad day, only THIS pool backs up, not your whole application.
 *
 * SIZING RATIONALE (the part interviewers actually probe on):
 *   These threads are I/O-bound (waiting on network calls), not CPU-bound.
 *   For I/O-bound work the rule of thumb is:
 *
 *       pool size = cores * (1 + wait_time / compute_time)
 *
 *   Since wait_time >> compute_time for a network call, you can size well
 *   above your core count — unlike a CPU-bound pool which you'd cap near
 *   core count to avoid context-switch overhead.
 *
 *   The numbers below (10 core / 20 max / 100 queue) are a reasonable
 *   starting point for a moderate-traffic service. In a real system you'd
 *   tune this against:
 *     - expected concurrent request volume
 *     - the external API's own rate limit / connection capacity
 *       (no point sizing your pool to 50 if the upstream API only allows
 *       10 concurrent connections — you'll just get throttled or 429'd)
 *   ...then validate with load testing.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "externalApiExecutor")
    public Executor externalApiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ext-api-");

        // What happens when core+max threads are busy AND the queue (100) is full?
        // Default policy throws TaskRejectedException, which would surface as a
        // 500 to the caller. CallerRunsPolicy is often safer for external-API
        // pools: it makes the calling thread execute the task itself, which
        // naturally slows down the caller instead of failing outright —
        // a simple form of backpressure.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
