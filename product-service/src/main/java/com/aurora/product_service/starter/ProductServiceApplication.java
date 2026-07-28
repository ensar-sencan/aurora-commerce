package com.aurora.product_service.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// Her iki güvenlik sınıfını da (Ana kapı ve Şifre üretici) devre dışı bırakıyoruz
@SpringBootApplication(scanBasePackages = "com.aurora.product_service")
@EnableJpaRepositories(basePackages = "com.aurora.product_service.repo")
@EntityScan(basePackages = "com.aurora.product_service.domain")
@EnableCaching  //cache mekanizmasını aktif etmek için
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

}
