package com.aurora.order_service.repo;

import com.aurora.order_service.domain.Cart;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends CrudRepository<Cart, Long> {
    // CrudRepository bize default olarak save(), findById(), deleteById() gibi metotları bedavaya verir.
    // Sepet bulmak için ekstra bir metot yazmamıza gerek yok, id (customerId) ile findById() kullanacağız.
}
