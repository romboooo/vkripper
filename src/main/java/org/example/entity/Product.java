package org.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "available", nullable = false)
    private boolean available = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_group", nullable = false)
    private ProductGroup productGroup;

    @JsonIgnore
    @OneToMany(mappedBy = "product")
    private List<Review> reviews;

    @JsonIgnore
    @OneToMany(mappedBy = "product")
    private List<Favorite> favorites;

}
