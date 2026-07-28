package com.aurora.product_service.web;

import com.aurora.product_service.service.StockService;
import com.aurora.product_service.web.dto.StockDeductRequest;
import com.aurora.product_service.web.dto.StockDeductResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "internal/stock") //Bu endpoint dış dünyaya kapalıdır, yalnızca order-service buraya istek atabilir
public class InternalStockController { //Servisler arası kapı.

    private  final StockService stockService;

    public InternalStockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/deduct")
    public ResponseEntity<StockDeductResponse> deduct(@RequestBody StockDeductRequest request) {
        StockDeductResponse response = stockService.deduct(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/restore")
    public ResponseEntity<Void> restore(@RequestBody StockDeductRequest request) {
        stockService.restore(request);
        return ResponseEntity.ok().build();
    }
}
