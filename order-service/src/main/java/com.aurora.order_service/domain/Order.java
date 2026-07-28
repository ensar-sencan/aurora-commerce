package com.aurora.order_service.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders" , schema ="orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @Column(name = "customer_id" , nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long total;

    @Column(nullable = false)
    private String status="pending";

    @Column(name = "idempotency_key" , unique = true)
    private String idempotencyKey;

    @OneToMany(mappedBy = "order" , cascade = CascadeType.ALL , orphanRemoval = true)
    private List<OrderItem> items = new  ArrayList<>();  // fişin icerisinde birden fazla urunn list oalrak alicaz
}
