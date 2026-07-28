package com.aurora.order_service.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items" , schema = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id" , nullable = false)
    @JsonIgnore  //Ürünü JSON'a çevirirken içindeki fatura detayını es geç, sadece ürün bilgilerini ver der
    private Order order;

    @Column(name = "product_id" , nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name ="unit_price" , nullable = false)
    private Long unitPrice;
}
