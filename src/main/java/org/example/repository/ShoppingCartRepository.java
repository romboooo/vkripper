package org.example.repository;

import org.example.entity.ShoppingCart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    Page<ShoppingCart> findByUserId(Long userId, Pageable pageable);
    List<ShoppingCart> findAllByUserId(Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    List<ShoppingCart> findByUserIdAndProductId(Long userId, Long productId);
}
