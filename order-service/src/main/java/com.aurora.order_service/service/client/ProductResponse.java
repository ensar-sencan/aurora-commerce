package com.aurora.order_service.service.client;

public record ProductResponse(Long id, String name, Long unitPrice, Integer stock) {}