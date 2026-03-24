package org.example.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {
    private Long orderId;
    private String status;
    private String message;
    private String redirectUrl;
}