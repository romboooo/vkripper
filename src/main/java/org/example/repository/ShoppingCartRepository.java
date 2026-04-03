package org.example.repository;

import org.example.entity.Product;
import org.example.entity.ShoppingCart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    Page<ShoppingCart> findByUserUsername(String username, Pageable pageable);
    List<ShoppingCart> findAllByUserUsername(String username);

    boolean existsByUserUsernameAndProductId(String username, Long productId);

    List<ShoppingCart> findByUserUsernameAndProductId(String username, Long productId);

    ShoppingCart findFirstByUserUsernameAndProductId(String username, Long productId);
}
