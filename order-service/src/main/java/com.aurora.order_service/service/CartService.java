package com.aurora.order_service.service;

import com.aurora.order_service.domain.Cart;
import com.aurora.order_service.domain.CartItem;
import com.aurora.order_service.repo.CartRepository;
import com.aurora.order_service.service.client.ProductClient;
import com.aurora.order_service.service.client.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient;

    // 1. Müşterinin Sepetini Getir
    public Cart getCart(Long customerId) {
        return cartRepository.findById(customerId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomerId(customerId);
                    return newCart;
                });
    }

    // 2. Sepete Ürün Ekle
    public Cart addToCart(Long customerId, CartItem newItem) {

        // Artık FakeStoreAPI'ye değil, KENDİ product-service'imize soruyoruz!
        ProductResponse realProduct;
        try {
            realProduct = productClient.getProductById(newItem.getProductId());
        } catch (Exception e) {
            throw new RuntimeException("Ürün bulunamadı! Katalogda böyle bir ürün yok.");
        }

        // Fiyatı doğrudan kendi veritabanımızdaki güncel fiyatla (Long) eziyoruz.
        newItem.setUnitPrice(realProduct.unitPrice());

        // Önce müşterinin mevcut sepetini bul (veya yeni oluştur)
        Cart cart = getCart(customerId);

        // Java Stream API ile bakıyoruz: Eklenmek istenen ürün zaten sepette var mı?
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(newItem.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Ürün sepette zaten varsa, sadece adedini (quantity) artır
            existingItem.get().setQuantity(existingItem.get().getQuantity() + newItem.getQuantity());
        } else {
            // Ürün sepette yoksa, yepyeni bir satır olarak sepete ekle
            cart.getItems().add(newItem);
        }

        // Güncellenmiş sepeti tekrar Redis'e kaydet ve geri dön
        return cartRepository.save(cart);
    }

    // 3. Sepeti Temizle
    public void ClearCart(Long customerId) {
        cartRepository.deleteById(customerId);
    }
}