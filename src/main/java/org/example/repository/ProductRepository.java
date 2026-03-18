package org.example.repository;

import org.example.entity.Product;
import org.example.entity.ProductGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // если че findAll и findById у тебя уже есть в JpaRepository
    // так что переопределять не стал

    List<Product> findByProductGroup(ProductGroup group);

    List<Product> findByAvailableTrue();

    List<Product> findByAvailableFalse();

    List<Product> findByProductGroupAndAvailable(ProductGroup group, boolean available);
}
