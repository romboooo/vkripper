package org.example.dto.request;

import lombok.Data;

@Data
public class ShoppingCartRequest {
    private Long userId;
    private Long productId;
}
