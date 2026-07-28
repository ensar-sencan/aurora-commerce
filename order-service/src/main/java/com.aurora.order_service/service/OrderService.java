package com.aurora.order_service.service;

import com.aurora.order_service.domain.Cart;
import com.aurora.order_service.domain.Order;
import com.aurora.order_service.domain.OrderItem;
import com.aurora.order_service.repo.OrderRepository;
import com.aurora.order_service.service.client.ProductClient;
import com.aurora.order_service.service.client.StockDeductRequest;
import com.aurora.order_service.service.client.StockDeductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    // application.yml içindeki gizli şifremizi alıyoruz
    @Value("${product-service.internal-token:local-internal-token}")
    private String internalToken;

    public Order checkout(Long customerId, String idempotencyKey) {

        // 1. ADIM: Müşterinin sepetini Redis'ten al
        Cart cart = cartService.getCart(customerId);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Sepet boş, ödeme yapılamaz.");
        }

        // 2. ADIM: Sepetteki ürünleri product-service'in anlayacağı formata (DTO) çevir
        List<StockDeductRequest.Line> requestLines = cart.getItems().stream()
                .map(item -> new StockDeductRequest.Line(item.getProductId(), item.getQuantity()))
                .toList();
        StockDeductRequest deductRequest = new StockDeductRequest(requestLines);

        // 3. ADIM: Ürün servisine git ve stokları düş!
        StockDeductResponse deductResponse;
        try {
            deductResponse = productClient.deduct(internalToken, deductRequest);
        } catch (Exception e) {
            // Eğer stok yetmezse (409 dönerse) sistem burada durur, sipariş oluşmaz.
            throw new RuntimeException("Stok yetersiz veya ürün bulunamadı! Sipariş iptal edildi.");
        }

        // 4. ADIM: Siparişi DB'ye yazmayı dene (SAGA TELAFİ BAŞLANGICI)
        try {
            Order order = new Order();
            order.setCustomerId(customerId);
            order.setIdempotencyKey(idempotencyKey);
            order.setStatus("pending");

            // Eğer Entity'nde items listesi boş başlatılmamışsa, NullPointerException almamak için:
            if (order.getItems() == null) {
                order.setItems(new ArrayList<>());
            }

            long total = 0L;

            // product-service bize GÜNCEL fiyatları verdi. Kendi sepetimize güvenmiyoruz, onları baz alıyoruz!
            for (StockDeductResponse.PricedLine pricedLine : deductResponse.lines()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order); // İlişkiyi bağlıyoruz
                orderItem.setProductId(pricedLine.productId());
                orderItem.setQuantity(pricedLine.quantity());
                orderItem.setUnitPrice(pricedLine.unitPrice());

                order.getItems().add(orderItem);
                total += (pricedLine.unitPrice() * pricedLine.quantity());
            }
            order.setTotal(total);

            // saveAndFlush kritik: DB hatası (örn. Unique Idempotency Key) varsa hemen fırlatır!
            Order savedOrder = orderRepository.saveAndFlush(order);

            // 5. ADIM: Her şey harika! Müşterinin sepetini Redis'ten temizle
            cartService.ClearCart(customerId);

            return savedOrder;

        } catch (Exception e) {
            // SAGA PATLADI! Veritabanı çöktü ya da aynı sipariş 2 kere tıklandı.
            // HEMEN TELAFİ ET (COMPENSATING TRANSACTION) VE STOKLARI GERİ VER.
            productClient.restore(internalToken, deductRequest);

            throw new RuntimeException("Siparişiniz kaydedilirken sistemsel bir hata oluştu. Müşteri hakkı korundu, stoklar iade edildi!", e);
        }
    }
    // Müşterinin geçmiş siparişlerini getir
    public List<Order> getMyOrders(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
}