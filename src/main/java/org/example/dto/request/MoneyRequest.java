package org.example.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public abstract class MoneyRequest {
    BigDecimal moneyAmount;
}
