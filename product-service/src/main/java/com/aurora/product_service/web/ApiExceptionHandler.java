package com.aurora.product_service.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<Map<String, Object>> handleOutOfStock(OutOfStockException ex) {
        // Stok bittiğinde 409 Conflict ve spec'e uygun JSON dönüyoruz[cite: 1]
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "out_of_stock", "productId", ex.getProductId()));
    }
}