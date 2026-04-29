package org.example.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopUpRequest extends MoneyRequest{

    @Min(0)
    BigDecimal moneyAmount;
}
