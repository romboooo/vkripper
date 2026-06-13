package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PurchaseProcessStartResponse {
    private String status;
    private String businessKey;
    private String processInstanceId;
}
