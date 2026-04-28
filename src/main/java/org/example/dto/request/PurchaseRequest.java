package org.example.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.entity.PurchaseType;

import java.math.BigDecimal;

@Data
public class PurchaseRequest {
    @NotNull
    private Long cartItemId;

    @NotNull
    private PurchaseType purchaseType;

    @Min(1)
    private int amountInPurchase;
}
