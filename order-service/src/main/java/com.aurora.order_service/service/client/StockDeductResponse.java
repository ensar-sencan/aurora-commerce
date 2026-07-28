package com.aurora.order_service.service.client;

import java.util.List;

public record StockDeductResponse(List<PricedLine> lines) {
    public record PricedLine(Long productId, Integer quantity, Long unitPrice) {}
}