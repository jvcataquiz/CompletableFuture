package com.coffee.cfdemo;

import com.coffee.cfdemo.dto.OrderDetails;
import com.coffee.cfdemo.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OrderServiceTimingTest {

    @Autowired
    private OrderService orderService;

    @Test
    void sequentialCallShouldTakeRoughlyTheSumOfAllLatencies() {
        OrderDetails result = orderService.getOrderDetailsSequential("test-1");

        // 200 + 300 + 250 = 750ms expected, allow some JVM scheduling slack
        assertTrue(result.getTotalTimeMs() >= 700,
                "Sequential call should take roughly the sum of all 3 latencies, took: "
                        + result.getTotalTimeMs() + "ms");
    }

    @Test
    void parallelCallShouldTakeRoughlyTheSlowestSingleLatency() {
        OrderDetails result = orderService.getOrderDetailsParallel("test-2");

        // max(200, 300, 250) = 300ms expected, allow some slack for thread startup
        assertTrue(result.getTotalTimeMs() < 700,
                "Parallel call should take roughly the slowest single call (~300ms), took: "
                        + result.getTotalTimeMs() + "ms");
    }

    @Test
    void bothStrategiesShouldReturnTheSameData() {
        OrderDetails sequential = orderService.getOrderDetailsSequential("test-3");
        OrderDetails parallel = orderService.getOrderDetailsParallel("test-3");

        assertEquals(sequential.getUser().getName(), parallel.getUser().getName());
        assertEquals(sequential.getPayment().getStatus(), parallel.getPayment().getStatus());
        assertEquals(sequential.getShipping().getCarrier(), parallel.getShipping().getCarrier());
    }
}
