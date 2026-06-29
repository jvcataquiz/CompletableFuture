# CompletableFuture Demo — Optimizing Slow External API Calls

A minimal Spring Boot project demonstrating the standard interview answer to:

> "A microservice is slow because of external API calls — how would you optimize it?"

This project calls the same 3 simulated external services (User, Payment, Shipping)
two different ways, so you can directly compare the timing:

| Endpoint | Strategy | Expected total time |
|---|---|---|
| `GET /api/orders/{orderId}/sequential` | One call after another | ~750ms (200+300+250) |
| `GET /api/orders/{orderId}/parallel` | `CompletableFuture`, concurrent | ~300ms (max of the three) |

The point: when calls are **independent** of each other, sequential execution wastes
time adding up latencies that should be overlapping instead.

---

## Project structure

```
completablefuture-demo/
├── pom.xml
├── README.md                          <- you are here
└── src/
    ├── main/java/com/coffee/cfdemo/
    │   ├── CompletableFutureDemoApplication.java
    │   ├── config/
    │   │   └── AsyncConfig.java        <- dedicated thread pool setup (the "why")
    │   ├── client/                     <- simulated external API calls
    │   │   ├── UserServiceClient.java      (200ms simulated latency)
    │   │   ├── PaymentServiceClient.java   (300ms simulated latency)
    │   │   └── ShippingServiceClient.java  (250ms simulated latency)
    │   ├── model/                      <- User, Payment, Shipping
    │   ├── dto/
    │   │   └── OrderDetails.java        <- aggregate response w/ totalTimeMs
    │   ├── service/
    │   │   └── OrderService.java        <- THE CORE FILE: sequential vs parallel
    │   └── controller/
    │       └── OrderController.java     <- exposes both endpoints
    ├── main/resources/
    │   └── application.properties
    └── test/java/com/coffee/cfdemo/
        └── OrderServiceTimingTest.java  <- asserts the timing difference is real
```

**Start here:** `OrderService.java` — it has both methods side by side with inline
comments explaining every decision. `AsyncConfig.java` is the second-most important
file — it explains why we use a dedicated thread pool instead of the JVM default.

---

## How to run it

Requires **Java 17+** and **Maven**.

```bash
cd completablefuture-demo
mvn spring-boot:run
```

App starts on `http://localhost:8080`.

### Try it

```bash
# Slow version — watch the response time
curl http://localhost:8080/api/orders/ORD-1001/sequential

# Fast version — same data, much faster
curl http://localhost:8080/api/orders/ORD-1001/parallel
```

Compare the `totalTimeMs` field in each JSON response. Also watch the console logs —
you'll see the sequential version's log lines appear one after another with visible
gaps, while the parallel version's three "Calling X Service..." lines appear almost
simultaneously (different thread names: `ext-api-1`, `ext-api-2`, `ext-api-3`).

### Run the tests

```bash
mvn test
```

`OrderServiceTimingTest` asserts:
- Sequential call takes ≥700ms (sum of all 3 simulated latencies)
- Parallel call takes <700ms (bounded by the slowest single call, ~300ms)
- Both strategies return identical data — optimizing the *how* doesn't change the *what*

---

## The optimization techniques shown here

### 1. Dedicated thread pool (`AsyncConfig.java`)

```java
@Bean(name = "externalApiExecutor")
public Executor externalApiExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(100);
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
}
```

**Why not just use `CompletableFuture.supplyAsync(task)` with no executor?**
It defaults to `ForkJoinPool.commonPool()`, which is shared across your entire JVM.
A slow external dependency can then starve threads needed by unrelated parts of your
app. A dedicated pool per external dependency is the same idea as the **bulkhead**
resilience pattern — isolate the blast radius.

**Why these specific numbers (10/20/100)?**
These threads are I/O-bound (waiting on network responses), not CPU-bound, so you can
safely size the pool well above your CPU core count — the rule-of-thumb formula is:

```
pool size = cores × (1 + wait_time / compute_time)
```

In a real system, you'd also cap this against the **external API's own concurrent
connection limit** — no point sizing your pool to 50 if the upstream API only allows
10 concurrent connections, you'll just get rate-limited. Tune with load testing.

### 2. Parallel execution (`OrderService.getOrderDetailsParallel`)

```java
CompletableFuture<User> userFuture = CompletableFuture
    .supplyAsync(() -> userServiceClient.getUser(orderId), externalApiExecutor)
    .orTimeout(2, TimeUnit.SECONDS)
    .exceptionally(ex -> User.unavailable());
```

Three independent calls fired concurrently instead of one-after-another.
`CompletableFuture.allOf(...)` waits for all three, then `.join()` combines results.
Total wall-clock time becomes the duration of the **slowest single call**, not the sum.

### 3. Timeouts (`orTimeout`)

Without an explicit timeout, a hanging external API call could block its thread
indefinitely. `.orTimeout(2, TimeUnit.SECONDS)` (Java 9+) caps how long we wait.

> Note: `orTimeout()` completes the future exceptionally when the timeout expires,
> but it does not automatically cancel the underlying task submitted to the executor.
> In this demo, the simulated service calls may still finish later and log their
> responses after the endpoint has already returned.

### 4. Fallbacks (`exceptionally`)

```java
.exceptionally(ex -> {
    log.warn("User service call failed or timed out: {}", ex.getMessage());
    return User.unavailable();
});
```

If one call fails or times out, we substitute a fallback value instead of failing the
*entire* response. The other two calls' results are still returned. This is graceful
degradation — partial data beats no data.

In the current demo run, the timeout warning may show `null` because the
`TimeoutException` produced by `orTimeout()` does not always include a message.
That is why you can see logs like `User service call failed or timed out: null` even
though the service task itself may still complete later on its executor thread.

---

## What this demo intentionally leaves out (and why)

To keep the project focused and runnable without external infrastructure, this demo
does **not** include:

- **Circuit breaker (Resilience4j)** — would stop calling a consistently-failing
  service for a cooldown period instead of retrying every request. Natural next step
  if you want to extend this project.
- **Redis caching** — useful when the same external data is requested repeatedly and
  doesn't change often. Not modeled here since each simulated call already returns
  fast (relatively) and deterministic data.
- **Real HTTP clients** — `UserServiceClient` etc. use `Thread.sleep()` to simulate
  network latency instead of calling real APIs via `RestTemplate`/`WebClient`, so the
  project runs standalone with zero external dependencies or API keys.

Mentioning these naturally in an interview — "here's what I built, and here's what
I'd add next for production" — tends to land better than over-engineering a demo.

---

## Quick interview recap

> "I'd first confirm the external calls are actually independent of each other. If
> they are, I'd parallelize them with `CompletableFuture.supplyAsync()` on a
> dedicated thread pool — not the shared `ForkJoinPool.commonPool()`, to avoid one
> slow dependency starving the rest of the app. Each call gets its own timeout via
> `orTimeout()` so we never wait forever, and a fallback via `exceptionally()` so one
> failing call doesn't take down the whole response. That turns total latency from
> the *sum* of all calls into the *max* of all calls."
