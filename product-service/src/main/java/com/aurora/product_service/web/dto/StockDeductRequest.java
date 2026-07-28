package com.aurora.product_service.web.dto;

import javax.sound.sampled.Line;
import java.util.List;

public record StockDeductRequest(List<Line> lines) {
    public record  Line(Long productId , Integer quantity) {}
}
