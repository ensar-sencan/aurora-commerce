package com.aurora.product_service.web.dto;

import java.util.List;

public record StockDeductResponse(List<PricedLine> lines) {
    public record PricedLine(Long productId, Integer quantity, Long unitPrice) {}
}