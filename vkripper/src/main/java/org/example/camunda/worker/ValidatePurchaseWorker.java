package org.example.camunda.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.camunda.externalTask.CamundaExternalTask;
import org.example.camunda.externalTask.CamundaExternalTaskClient;
import org.example.entity.PurchaseType;
import org.example.service.PurchaseServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.example.camunda.externalTask.CamundaExternalTaskVariables.integerVariable;
import static org.example.camunda.externalTask.CamundaExternalTaskVariables.longVariable;
import static org.example.camunda.externalTask.CamundaExternalTaskVariables.stringVariable;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidatePurchaseWorker {
    private static final String VALIDATION_OK = "OK";
    private static final String VALIDATION_FAILED = "FAILED";

    private final CamundaWorkerProperties properties;
    private final CamundaExternalTaskClient externalTaskClient;
    private final PurchaseServiceImpl purchaseService;

    @Scheduled(fixedDelayString = "${app.camunda.worker.poll-delay:5000}")
    public void poll() {
        if (!properties.isEnabled()) {
            return;
        }

        List<CamundaExternalTask> tasks;
        try {
            tasks = externalTaskClient.fetchAndLock(
                    properties.getId(),
                    properties.getValidatePurchaseTopic(),
                    1,
                    properties.getLockDuration()
            );
        } catch (RuntimeException e) {
            log.warn("Failed to fetch Camunda external tasks for topic={}", properties.getValidatePurchaseTopic(), e);
            return;
        }

        for (CamundaExternalTask task : tasks) {
            handleTask(task);
        }
    }

    private void handleTask(CamundaExternalTask task) {
        try {
            validate(task);
        } catch (RuntimeException e) {
            completeBusinessFailure(task, e.getMessage());
            return;
        }

        try {
            externalTaskClient.complete(
                    task.getId(),
                    properties.getId(),
                    Map.of("validationStatus", VALIDATION_OK)
            );
            log.info("Completed validate-purchase external task id={}, businessKey={}", task.getId(), task.getBusinessKey());
        } catch (RuntimeException e) {
            handleTechnicalFailure(task, e);
        }
    }

    private void validate(CamundaExternalTask task) {
        Long buyerId = longVariable(task, "buyerId");
        Long cartItemId = longVariable(task, "cartItemId");
        PurchaseType purchaseType = PurchaseType.valueOf(stringVariable(task, "purchaseType"));
        Integer amountInPurchase = integerVariable(task, "amountInPurchase");

        purchaseService.validatePurchaseForProcess(buyerId, cartItemId, purchaseType, amountInPurchase);
    }

    private void completeBusinessFailure(CamundaExternalTask task, String errorMessage) {
        try {
            externalTaskClient.complete(
                    task.getId(),
                    properties.getId(),
                    Map.of(
                            "validationStatus", VALIDATION_FAILED,
                            "errorMessage", errorMessage == null ? "Purchase validation failed" : errorMessage
                    )
            );
            log.info(
                    "Completed validate-purchase external task with validation failure id={}, businessKey={}, errorMessage={}",
                    task.getId(),
                    task.getBusinessKey(),
                    errorMessage
            );
        } catch (RuntimeException e) {
            handleTechnicalFailure(task, e);
        }
    }

    private void handleTechnicalFailure(CamundaExternalTask task, RuntimeException e) {
        try {
            externalTaskClient.handleFailure(
                    task.getId(),
                    properties.getId(),
                    "Technical error while validating purchase",
                    e.getMessage()
            );
        } catch (RuntimeException failureException) {
            log.warn("Failed to report Camunda external task failure id={}", task.getId(), failureException);
        }
    }
}
