package org.example.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.entity.PurchaseType;

@Data
public class PurchaseRequest {
    @NotNull
    private Long cartItemId;

    @NotNull
    private PurchaseType purchaseType;
}
