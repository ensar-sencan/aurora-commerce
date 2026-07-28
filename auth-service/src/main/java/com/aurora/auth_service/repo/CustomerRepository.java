package com.aurora.auth_service.repo;

import com.aurora.auth_service.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository  extends JpaRepository<Customer , Long> {

    // Spring Data JPA bu metot ismine bakıp arka planda "SELECT * FROM auth.customers WHERE email = ?" SQL'ini yazar.
    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);
}
