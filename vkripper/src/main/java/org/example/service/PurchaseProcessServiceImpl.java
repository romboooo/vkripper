package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.camunda.process.CamundaProcessClient;
import org.example.camunda.process.CamundaProcessInstance;
import org.example.camunda.process.CamundaProcessStartException;
import org.example.camunda.CamundaProperties;
import org.example.dto.request.PurchaseRequest;
import org.example.dto.response.PurchaseProcessStartResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseProcessServiceImpl implements PurchaseProcessService {
    private static final String PROCESS_STARTED = "PROCESS_STARTED";

    private final CamundaProperties camundaProperties;
    private final CamundaProcessClient camundaProcessClient;

    @Override
    public PurchaseProcessStartResponse startPurchaseProcess(Long buyerId, PurchaseRequest request) {
        String processKey = camundaProperties.getPurchaseProcessKey();
        String businessKey = businessKey(buyerId, request.getCartItemId());
        Map<String, Object> variables = processVariables(buyerId, request);

        log.info(
                "Starting Camunda purchase process processKey={}, businessKey={}, buyerId={}, cartItemId={}, purchaseType={}, amountInPurchase={}",
                processKey,
                businessKey,
                buyerId,
                request.getCartItemId(),
                request.getPurchaseType(),
                request.getAmountInPurchase()
        );

        try {
            CamundaProcessInstance processInstance = camundaProcessClient.startProcess(
                    processKey,
                    businessKey,
                    variables
            );
            return new PurchaseProcessStartResponse(PROCESS_STARTED, businessKey, processInstance.id());
        } catch (RuntimeException e) {
            throw new CamundaProcessStartException(
                    "Failed to start purchase process in Camunda. Check that Camunda standalone is available at "
                            + camundaProperties.getBaseUrl(),
                    e
            );
        }
    }

    private String businessKey(Long buyerId, Long cartItemId) {
        return "purchase-%s-%s-%s".formatted(buyerId, cartItemId, Instant.now().toEpochMilli());
    }

    private Map<String, Object> processVariables(Long buyerId, PurchaseRequest request) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("buyerId", buyerId);
        variables.put("cartItemId", request.getCartItemId());
        variables.put("purchaseType", request.getPurchaseType().name());
        variables.put("amountInPurchase", request.getAmountInPurchase());
        return variables;
    }
}
