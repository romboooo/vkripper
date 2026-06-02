package org.example.repository;

import org.example.entity.ShoppingCartUpdate;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ShoppingCartUpdateRepository extends JpaRepository<ShoppingCartUpdate, Long> {
    ShoppingCartUpdate findFirstByUser(User user);

    List<ShoppingCartUpdate> findByLastUpdatedAtBefore(LocalDateTime updatedBefore);
    void deleteAllByUser(User user);
}
