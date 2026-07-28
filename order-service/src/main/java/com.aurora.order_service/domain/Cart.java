package com.aurora.order_service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.ArrayList;
import java.util.List;

@RedisHash("cart")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    @Id
    private  Long customerId; // Sepetin sahibi (Token'dan gelecek)

    private List<CartItem> items = new ArrayList<>();

    @TimeToLive
    private Long ttl = 86400L; // Sepetin ömrü 24 saat (Saniye cinsinden)


}
