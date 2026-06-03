package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="shopping_cart_update")
@Data
@RequiredArgsConstructor
public class ShoppingCartUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

}
