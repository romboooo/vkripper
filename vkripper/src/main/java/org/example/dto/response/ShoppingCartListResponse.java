package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.entity.ShoppingCart;
import org.springframework.data.domain.Page;
import java.util.List;

@AllArgsConstructor
@Data
public class ShoppingCartListResponse {
    private List<ShoppingCartResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static ShoppingCartListResponse fromPage(Page<ShoppingCart> page) {
        return new ShoppingCartListResponse(
                page.getContent().stream().map(ShoppingCartResponse::fromShoppingCart).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
