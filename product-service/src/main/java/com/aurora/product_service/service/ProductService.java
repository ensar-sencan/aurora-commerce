package com.aurora.product_service.service;

import com.aurora.product_service.domain.Product;
import com.aurora.product_service.repo.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public  ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    @Cacheable(value = "products2")    // Ürün listesini Redis'te cache'ler
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(()-> new  RuntimeException("product_not_found"));
    }
}
