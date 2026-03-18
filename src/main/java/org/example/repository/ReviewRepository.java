package org.example.repository;

import org.example.entity.Product;
import org.example.entity.Review;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUser(User user);

    List<Review> findByProduct(Product product);
    boolean existsByUserAndProduct(User user, Product product);
}
