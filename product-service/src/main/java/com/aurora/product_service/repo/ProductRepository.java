package com.aurora.product_service.repo;

import com.aurora.product_service.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Stok düşme sorgusu: Etkilenen satır sayısını (1 veya 0) döner.
    @Modifying
    @Query(
            value = """
            UPDATE product.product 
            SET stock = stock - :qty
            WHERE id = :id AND stock >= :qty
        """,
            nativeQuery = true
    )
    int deductStock(@Param("id") Long id, @Param("qty") Integer qty);

    // Saga telafisi (restore) için stok iade sorgusu
    @Modifying
    @Query(
            value = """
            UPDATE product.product 
            SET stock = stock + :qty
            WHERE id = :id
        """,
            nativeQuery = true
    )
    int restoreStock(@Param("id") Long id, @Param("qty") Integer qty);
}