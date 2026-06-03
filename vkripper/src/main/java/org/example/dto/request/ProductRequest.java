package org.example.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.entity.ProductGroup;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotNull
    private String name;

    @NotNull
    @Min(1)
    private BigDecimal price;

    private boolean available;

    @NotNull
    private ProductGroup productGroup;
}
