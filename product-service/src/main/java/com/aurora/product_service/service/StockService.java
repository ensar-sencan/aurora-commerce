package com.aurora.product_service.service;

import com.aurora.product_service.domain.Product;
import com.aurora.product_service.repo.ProductRepository;
import com.aurora.product_service.web.dto.StockDeductRequest;
import com.aurora.product_service.web.dto.StockDeductResponse;
import com.aurora.product_service.web.OutOfStockException;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockService {

    private final ProductRepository productRepository;

    public StockService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true) // Stok değiştiği için Redis cache'ini düşürüyoruz
    public StockDeductResponse deduct(StockDeductRequest request) {
        List<StockDeductResponse.PricedLine> pricedLines = new ArrayList<>();

        for (StockDeductRequest.Line line : request.lines()) {
            // 1. Adım: Ürünü ve gerçek birim fiyatını güvenle çekiyoruz
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new OutOfStockException(line.productId()));

            // 2. Adım: Stoğu güvenli bir şekilde düşüyoruz (Dönen değer: Etkilenen satır sayısı 1 veya 0)
            int updatedRows = productRepository.deductStock(line.productId(), line.quantity());

            if (updatedRows == 0) {
                // Stok yetersizse -> Tüm transaction rollback olur, sipariş iptal edilir
                throw new OutOfStockException(line.productId());
            }

            // 3. Adım: Gerçek ürün fiyatıyla PricedLine nesnesini oluşturuyoruz
            pricedLines.add(new StockDeductResponse.PricedLine(line.productId(), line.quantity(), product.getUnitPrice()));
        }

        return new StockDeductResponse(pricedLines);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void restore(StockDeductRequest request) {
        for (StockDeductRequest.Line line : request.lines()) {
            productRepository.restoreStock(line.productId(), line.quantity());
        }
    }


}



