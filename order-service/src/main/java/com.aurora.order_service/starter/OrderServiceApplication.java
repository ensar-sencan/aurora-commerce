package com.aurora.order_service.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;


// 1. Service, Controller ve Config sınıfları için genel radar
@SpringBootApplication(scanBasePackages = "com.aurora.order_service")

// 2. PostgreSQL Repository (OrderRepository) için radar
@EnableJpaRepositories(basePackages = "com.aurora.order_service.repo")

// 3. Redis Repository (CartRepository) için radar
@EnableRedisRepositories(basePackages = "com.aurora.order_service.repo")

// 4. JPA Varlıkları (@Entity ve @RedisHash) sınıfları için radar
@EntityScan(basePackages = "com.aurora.order_service.domain")

@EnableFeignClients(basePackages = "com.aurora.order_service")
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}

