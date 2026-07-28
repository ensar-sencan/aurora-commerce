package com.aurora.order_service.repo;

import com.aurora.order_service.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order , Long> {

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    // Müşteri ID'sine göre siparişleri bul
    List<Order> findByCustomerId(Long customerId);
}
