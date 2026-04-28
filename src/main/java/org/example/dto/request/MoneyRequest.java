package org.example.dto.request;

import jakarta.validation.constraints.Min;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MoneyRequest {

    @Min(0)
    private BigDecimal amount;
}
