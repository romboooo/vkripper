package org.example.dto.request;

import lombok.Data;
import org.example.entity.Product;
import org.example.entity.User;

@Data
public class ShoppingCartRequest {
    private Long id;
    private int amount;
    private Product product;
    private User user;
}
