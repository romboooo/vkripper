package org.example.camunda.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.camunda.externalTask.CamundaExternalTask;
import org.example.camunda.externalTask.CamundaExternalTaskClient;
import org.example.entity.PurchaseType;
import org.example.service.BalancePurchaseProcessStepResult;
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
public class CreateBalancePurchaseWorker {
    private static final String CREATION_OK = "OK";
    private static final String CREATION_FAILED = "FAILED";
    private static final String CREATION_SKIPPED = "SKIPPED";

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
                    properties.getCreateBalancePurchaseTopic(),
                    1,
                    properties.getLockDuration()
            );
        } catch (RuntimeException e) {
            log.warn("Failed to fetch Camunda external tasks for topic={}", properties.getCreateBalancePurchaseTopic(), e);
            return;
        }

        for (CamundaExternalTask task : tasks) {
            handleTask(task);
        }
    }

    private void handleTask(CamundaExternalTask task) {
        Long buyerId;
        Long cartItemId;
        PurchaseType purchaseType;
        Integer amountInPurchase;

        try {
            buyerId = longVariable(task, "buyerId");
            cartItemId = longVariable(task, "cartItemId");
            purchaseType = PurchaseType.valueOf(stringVariable(task, "purchaseType"));
            amountInPurchase = integerVariable(task, "amountInPurchase");
        } catch (RuntimeException e) {
            completeBusinessFailure(task, e.getMessage());
            return;
        }

        if (purchaseType != PurchaseType.BALANCE) {
            completeSkipped(task);
            return;
        }

        BalancePurchaseProcessStepResult result;
        try {
            result = purchaseService.createBalancePurchaseForProcess(buyerId, cartItemId, amountInPurchase);
        } catch (RuntimeException e) {
            completeBusinessFailure(task, e.getMessage());
            return;
        }

        try {
            externalTaskClient.complete(
                    task.getId(),
                    properties.getId(),
                    Map.of(
                            "purchaseCreationStatus", CREATION_OK,
                            "orderId", result.orderId(),
                            "paymentId", result.paymentId(),
                            "operationId", result.operationId()
                    )
            );
            log.info(
                    "Completed create-balance-purchase external task id={}, businessKey={}, orderId={}, paymentId={}, operationId={}",
                    task.getId(),
                    task.getBusinessKey(),
                    result.orderId(),
                    result.paymentId(),
                    result.operationId()
            );
        } catch (RuntimeException e) {
            handleTechnicalFailure(task, e);
        }
    }

    private void completeSkipped(CamundaExternalTask task) {
        try {
            externalTaskClient.complete(
                    task.getId(),
                    properties.getId(),
                    Map.of(
                            "purchaseCreationStatus", CREATION_SKIPPED,
                            "errorMessage", "Unsupported purchase type for create-balance-purchase"
                    )
            );
        } catch (RuntimeException e) {
            handleTechnicalFailure(task, e);
        }
    }

    private void completeBusinessFailure(CamundaExternalTask task, String errorMessage) {
        try {
            externalTaskClient.complete(
                    task.getId(),
                    properties.getId(),
                    Map.of(
                            "purchaseCreationStatus", CREATION_FAILED,
                            "errorMessage", errorMessage == null ? "Balance purchase creation failed" : errorMessage
                    )
            );
            log.info(
                    "Completed create-balance-purchase external task with failure id={}, businessKey={}, errorMessage={}",
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
                    "Technical error while creating balance purchase",
                    e.getMessage()
            );
        } catch (RuntimeException failureException) {
            log.warn("Failed to report Camunda external task failure id={}", task.getId(), failureException);
        }
    }
}
