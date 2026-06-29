package com.coffee.cfdemo.controller;

import com.coffee.cfdemo.dto.OrderDetails;
import com.coffee.cfdemo.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Two endpoints calling the SAME 3 external services, with different
 * orchestration strategies, so the timing difference can be compared
 * directly via Postman / curl / browser.
 *
 *   GET /api/orders/{orderId}/sequential  -> ~750ms  (200+300+250, summed)
 *   GET /api/orders/{orderId}/parallel    -> ~300ms  (max of 200,300,250)
 */
@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/api/orders/{orderId}/sequential")
    public OrderDetails getOrderSequential(@PathVariable String orderId) {
        return orderService.getOrderDetailsSequential(orderId);
    }

    @GetMapping("/api/orders/{orderId}/parallel")
    public OrderDetails getOrderParallel(@PathVariable String orderId) {
        return orderService.getOrderDetailsParallel(orderId);
    }
}
