package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.ShoppingCart;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ShoppingCartResponse {
    private Long id;
    private int amount;
    private ProductResponse product;
    private UserResponse user;

    public static ShoppingCartResponse fromShoppingCart(ShoppingCart shoppingCart){
        return new ShoppingCartResponse(
                shoppingCart.getId(),
                shoppingCart.getAmount(),
                ProductResponse.fromProduct(shoppingCart.getProduct()),
                UserResponse.fromUser(shoppingCart.getUser())
        );
    }
}
