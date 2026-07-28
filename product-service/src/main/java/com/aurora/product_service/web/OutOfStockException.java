package com.aurora.product_service.web;

public class OutOfStockException extends RuntimeException {

    private final Long productId;

    public OutOfStockException(Long productId) {
        super("out_of_stock"); // Spec dosyasına göre hata mesajımız
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}