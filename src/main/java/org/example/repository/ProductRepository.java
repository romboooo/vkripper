package org.example.repository;

import org.example.entity.Product;
import org.example.entity.ProductGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByProductGroup(ProductGroup group, Pageable pageable);

    Page<Product> findByAvailableTrue(Pageable pageable);

    Page<Product> findByAvailableFalse(Pageable pageable);

    List<Product> findByNameContainingIgnoreCaseAndAvailableTrue(String name);

    Page<Product> findByProductGroupAndAvailable(ProductGroup group, boolean available, Pageable pageable);
}
