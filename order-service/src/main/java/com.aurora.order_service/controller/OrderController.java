package com.aurora.order_service.controller;

import com.aurora.order_service.domain.Order;
import com.aurora.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Sepeti Siparişe Dönüştür (Checkout)
    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(
            // 1. Müşteri ID'sini doğrulanmış JWT'den (güvenli odadan) çekiyoruz
            @AuthenticationPrincipal Long customerId,

            // 2. Çift tıklama / tekrar hatalarını önlemek için ön yüzden gelen benzersiz şifreyi alıyoruz
            @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey
    ) {
        Order newOrder = orderService.checkout(customerId, idempotencyKey);
        return ResponseEntity.ok(newOrder);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getMyOrders(@AuthenticationPrincipal Long customerId) {
        List<Order> orders = orderService.getMyOrders(customerId);
        return ResponseEntity.ok(orders);
    }
}