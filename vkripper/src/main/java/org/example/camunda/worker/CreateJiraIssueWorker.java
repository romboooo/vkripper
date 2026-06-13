package org.example.camunda.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.camunda.externalTask.CamundaExternalTask;
import org.example.camunda.externalTask.CamundaExternalTaskClient;
import org.example.jira.CamundaJiraIssueService;
import org.example.jira.adapter.api.JiraIssueResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.example.camunda.externalTask.CamundaExternalTaskVariables.longVariable;
import static org.example.camunda.externalTask.CamundaExternalTaskVariables.optionalStringVariable;
import static org.example.camunda.externalTask.CamundaExternalTaskVariables.stringVariable;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateJiraIssueWorker {
    private static final String JIRA_OK = "OK";
    private static final String JIRA_FAILED = "FAILED";

    private final CamundaWorkerProperties properties;
    private final CamundaExternalTaskClient externalTaskClient;
    private final CamundaJiraIssueService jiraIssueService;

    @Scheduled(fixedDelayString = "${app.camunda.worker.poll-delay:5000}")
    public void poll() {
        if (!properties.isEnabled()) {
            return;
        }

        List<CamundaExternalTask> tasks;
        try {
            tasks = externalTaskClient.fetchAndLock(
                    properties.getId(),
                    properties.getCreateJiraIssueTopic(),
                    1,
                    properties.getLockDuration()
            );
        } catch (RuntimeException e) {
            log.warn("Failed to fetch Camunda external tasks for topic={}", properties.getCreateJiraIssueTopic(), e);
            return;
        }

        for (CamundaExternalTask task : tasks) {
            handleTask(task);
        }
    }

    private void handleTask(CamundaExternalTask task) {
        Long orderId;
        Long paymentId;
        Long operationId;
        String operationStatus;
        String errorCode;
        String errorMessage;

        try {
            orderId = longVariable(task, "orderId");
            paymentId = longVariable(task, "paymentId");
            operationId = longVariable(task, "operationId");
            operationStatus = stringVariable(task, "operationStatus");
            errorCode = optionalStringVariable(task, "errorCode");
            errorMessage = optionalStringVariable(task, "errorMessage");
        } catch (RuntimeException e) {
            completeBusinessFailure(task, "Missing required process variable: " + e.getMessage());
            return;
        }

        JiraIssueResult result;
        try {
            // TODO: Store jiraIssueKey in process/domain state before creating a new issue to avoid duplicates on task retry.
            result = jiraIssueService.createFinancialOperationProblemIssue(
                    orderId,
                    paymentId,
                    operationId,
                    operationStatus,
                    errorCode,
                    errorMessage
            );
        } catch (RuntimeException e) {
            completeBusinessFailure(task, e.getMessage());
            return;
        }

        if (!result.isCreated()) {
            completeBusinessFailure(task, result.getMessage());
            return;
        }

        try {
            Map<String, Object> variables = new LinkedHashMap<>();
            variables.put("jiraIssueStatus", JIRA_OK);
            variables.put("jiraIssueCreated", true);
            if (result.getIssueKey() != null && !result.getIssueKey().isBlank()) {
                variables.put("jiraIssueKey", result.getIssueKey());
            }

            externalTaskClient.complete(task.getId(), properties.getId(), variables);
            log.info(
                    "Completed create-jira-issue external task id={}, businessKey={}, operationId={}, jiraIssueKey={}",
                    task.getId(),
                    task.getBusinessKey(),
                    operationId,
                    result.getIssueKey()
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
                            "jiraIssueStatus", JIRA_FAILED,
                            "jiraIssueCreated", false,
                            "errorMessage", errorMessage == null ? "Jira issue creation failed" : errorMessage
                    )
            );
            log.info(
                    "Completed create-jira-issue external task with failure id={}, businessKey={}, errorMessage={}",
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
                    "Technical error while creating Jira issue",
                    e.getMessage()
            );
        } catch (RuntimeException failureException) {
            log.warn("Failed to report Camunda external task failure id={}", task.getId(), failureException);
        }
    }
}
