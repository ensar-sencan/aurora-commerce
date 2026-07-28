package com.aurora.order_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

// URL'i hardcoded vermek yerine application.yml'daki product-service.url değişkeninden alıyoruz
@FeignClient(name = "product-client", url = "${product-service.url}")
public interface ProductClient {

    // 1. Kendi servisimizden ürünün fiyatını öğrenmek için
    @GetMapping("/products/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id);

    // Stok düşüm endpoint'imiz
    @PostMapping("/internal/stock/deduct")
    StockDeductResponse deduct(
            @RequestHeader("X-Internal-Token") String internalToken,
            @RequestBody StockDeductRequest request
    );

    // Olası bir hatada stokları geri iade edeceğimiz (Saga telafi) endpoint'imiz
    @PostMapping("/internal/stock/restore")
    void restore(
            @RequestHeader("X-Internal-Token") String internalToken,
            @RequestBody StockDeductRequest request
    );
}