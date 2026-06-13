package org.example.camunda.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.camunda.externalTask.CamundaExternalTask;
import org.example.camunda.externalTask.CamundaExternalTaskClient;
import org.example.service.PurchaseServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.example.camunda.externalTask.CamundaExternalTaskVariables.longVariable;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublishFinancialOperationWorker {
    private static final String PUBLISH_OK = "OK";
    private static final String PUBLISH_FAILED = "FAILED";

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
                    properties.getPublishFinancialOperationTopic(),
                    1,
                    properties.getLockDuration()
            );
        } catch (RuntimeException e) {
            log.warn("Failed to fetch Camunda external tasks for topic={}", properties.getPublishFinancialOperationTopic(), e);
            return;
        }

        for (CamundaExternalTask task : tasks) {
            handleTask(task);
        }
    }

    private void handleTask(CamundaExternalTask task) {
        Long operationId;
        Long orderId;
        Long paymentId;

        try {
            operationId = longVariable(task, "operationId");
            orderId = longVariable(task, "orderId");
            paymentId = longVariable(task, "paymentId");
        } catch (RuntimeException e) {
            completeBusinessFailure(task, e.getMessage());
            return;
        }

        try {
            purchaseService.publishFinancialOperationForProcess(operationId);
        } catch (RuntimeException e) {
            completeBusinessFailure(task, e.getMessage());
            return;
        }

        try {
            externalTaskClient.complete(
                    task.getId(),
                    properties.getId(),
                    Map.of(
                            "financialOperationPublished", true,
                            "financialOperationPublishStatus", PUBLISH_OK
                    )
            );
            log.info(
                    "Completed publish-financial-operation external task id={}, businessKey={}, orderId={}, paymentId={}, operationId={}",
                    task.getId(),
                    task.getBusinessKey(),
                    orderId,
                    paymentId,
                    operationId
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
                            "financialOperationPublished", false,
                            "financialOperationPublishStatus", PUBLISH_FAILED,
                            "errorMessage", errorMessage == null ? "Financial operation publish failed" : errorMessage
                    )
            );
            log.info(
                    "Completed publish-financial-operation external task with failure id={}, businessKey={}, errorMessage={}",
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
                    "Technical error while publishing financial operation",
                    e.getMessage()
            );
        } catch (RuntimeException failureException) {
            log.warn("Failed to report Camunda external task failure id={}", task.getId(), failureException);
        }
    }
}
