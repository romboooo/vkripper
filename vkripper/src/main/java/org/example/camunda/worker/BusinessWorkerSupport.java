package org.example.camunda.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.camunda.externalTask.CamundaExternalTask;
import org.example.camunda.externalTask.CamundaExternalTaskClient;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessWorkerSupport {
    private static final String OK = "OK";
    private static final String FAILED = "FAILED";

    private final CamundaWorkerProperties properties;
    private final CamundaExternalTaskClient externalTaskClient;

    public void pollTopic(String topic, String statusVariable, TaskHandler handler) {
        List<CamundaExternalTask> tasks;
        try {
            tasks = externalTaskClient.fetchAndLock(properties.getId(), topic, 1, properties.getLockDuration());
        } catch (RuntimeException e) {
            log.warn("Failed to fetch Camunda external tasks for topic={}", topic, e);
            return;
        }

        for (CamundaExternalTask task : tasks) {
            handleTask(topic, statusVariable, handler, task);
        }
    }

    public Map<String, Object> ok(String statusVariable) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put(statusVariable, OK);
        return variables;
    }

    private void handleTask(String topic, String statusVariable, TaskHandler handler, CamundaExternalTask task) {
        Map<String, Object> variables;
        try {
            variables = handler.handle(task);
        } catch (RuntimeException e) {
            completeBusinessFailure(topic, statusVariable, task, e.getMessage());
            return;
        }

        try {
            externalTaskClient.complete(task.getId(), properties.getId(), variables);
            log.info("Completed Camunda external task topic={}, id={}, businessKey={}", topic, task.getId(), task.getBusinessKey());
        } catch (RuntimeException e) {
            handleTechnicalFailure(topic, task, e);
        }
    }

    private void completeBusinessFailure(String topic, String statusVariable, CamundaExternalTask task, String errorMessage) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put(statusVariable, FAILED);
        variables.put("errorMessage", errorMessage == null ? "Business operation failed" : errorMessage);
        try {
            externalTaskClient.complete(task.getId(), properties.getId(), variables);
            log.info("Completed Camunda external task with business failure topic={}, id={}", topic, task.getId());
        } catch (RuntimeException e) {
            handleTechnicalFailure(topic, task, e);
        }
    }

    private void handleTechnicalFailure(String topic, CamundaExternalTask task, RuntimeException e) {
        try {
            externalTaskClient.handleFailure(
                    task.getId(),
                    properties.getId(),
                    "Technical error while processing topic " + topic,
                    e.getMessage()
            );
        } catch (RuntimeException failureException) {
            log.warn("Failed to report Camunda external task failure id={}", task.getId(), failureException);
        }
    }

    public interface TaskHandler {
        Map<String, Object> handle(CamundaExternalTask task);
    }
}
