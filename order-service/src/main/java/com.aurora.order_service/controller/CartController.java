package com.aurora.order_service.controller;

import com.aurora.order_service.domain.Cart;
import com.aurora.order_service.domain.CartItem;
import com.aurora.order_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 1. Müşterinin kendi sepetini getirir
    @GetMapping
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal Long customerId) {
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    // 2. Müşterinin sepetine ürün ekler
    @PostMapping("/items")
    public ResponseEntity<Cart> addToCart(
            @AuthenticationPrincipal Long customerId,
            @RequestBody CartItem cartItem
    ) {
        return ResponseEntity.ok(cartService.addToCart(customerId, cartItem));
    }

    // 3. Müşterinin sepetini tamamen boşaltır
    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal Long customerId) {
        cartService.ClearCart(customerId);
        return ResponseEntity.ok().build();
    }
}