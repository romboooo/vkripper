package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.common.entity.FinancialOperation;
import org.example.common.enums.FinancialOperationStatus;
import org.example.common.enums.FinancialOperationType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialOperationResponse {
    private Long operationId;
    private FinancialOperationType type;
    private FinancialOperationStatus status;
    private String message;

    public static FinancialOperationResponse fromOperation(FinancialOperation operation, String message) {
        return new FinancialOperationResponse(
                operation.getId(),
                operation.getType(),
                operation.getStatus(),
                message
        );
    }
}
