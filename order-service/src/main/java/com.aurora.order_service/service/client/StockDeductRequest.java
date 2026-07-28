package com.aurora.order_service.service.client;

import java.util.List;

public record StockDeductRequest(List<Line> lines) {
    public record Line(Long productId, Integer quantity) {}
}