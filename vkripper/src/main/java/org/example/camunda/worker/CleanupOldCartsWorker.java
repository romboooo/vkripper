package org.example.camunda.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.camunda.externalTask.CamundaExternalTask;
import org.example.camunda.externalTask.CamundaExternalTaskClient;
import org.example.service.ShoppingCartService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.example.camunda.externalTask.CamundaExternalTaskVariables.optionalLongVariable;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupOldCartsWorker {
    private static final String CLEANUP_OK = "OK";
    private static final String CLEANUP_FAILED = "FAILED";

    private final CamundaWorkerProperties properties;
    private final CamundaExternalTaskClient externalTaskClient;
    private final ShoppingCartService shoppingCartService;

    @Scheduled(fixedDelayString = "${app.camunda.worker.poll-delay:5000}")
    public void poll() {
        if (!properties.isEnabled()) {
            return;
        }

        List<CamundaExternalTask> tasks;
        try {
            tasks = externalTaskClient.fetchAndLock(
                    properties.getId(),
                    properties.getCleanupOldCartsTopic(),
                    1,
                    properties.getLockDuration()
            );
        } catch (RuntimeException e) {
            log.warn("Failed to fetch Camunda external tasks for topic={}", properties.getCleanupOldCartsTopic(), e);
            return;
        }

        for (CamundaExternalTask task : tasks) {
            handleTask(task);
        }
    }

    private void handleTask(CamundaExternalTask task) {
        Long olderThanMinutes;
        try {
            olderThanMinutes = optionalLongVariable(task, "olderThanMinutes");
        } catch (RuntimeException e) {
            completeBusinessFailure(task, e.getMessage());
            return;
        }

        long thresholdMinutes = olderThanMinutes == null
                ? properties.getCleanupOldCartsOlderThanMinutes()
                : olderThanMinutes;
        if (thresholdMinutes < 1) {
            completeBusinessFailure(task, "olderThanMinutes must be greater than 0");
            return;
        }

        int deletedCartsCount;
        try {
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(thresholdMinutes);
            deletedCartsCount = shoppingCartService.deleteOldShoppingCarts(threshold);
        } catch (RuntimeException e) {
            completeBusinessFailure(task, e.getMessage());
            return;
        }

        try {
            externalTaskClient.complete(
                    task.getId(),
                    properties.getId(),
                    Map.of(
                            "cleanupStatus", CLEANUP_OK,
                            "deletedCartsCount", deletedCartsCount
                    )
            );
            log.info(
                    "Completed cleanup-old-carts external task id={}, businessKey={}, olderThanMinutes={}, deletedCartsCount={}",
                    task.getId(),
                    task.getBusinessKey(),
                    thresholdMinutes,
                    deletedCartsCount
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
                            "cleanupStatus", CLEANUP_FAILED,
                            "errorMessage", errorMessage == null ? "Shopping cart cleanup failed" : errorMessage
                    )
            );
            log.info(
                    "Completed cleanup-old-carts external task with failure id={}, businessKey={}, errorMessage={}",
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
                    "Technical error while cleaning old shopping carts",
                    e.getMessage()
            );
        } catch (RuntimeException failureException) {
            log.warn("Failed to report Camunda external task failure id={}", task.getId(), failureException);
        }
    }
}
