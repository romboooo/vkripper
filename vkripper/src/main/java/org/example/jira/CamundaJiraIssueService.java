package org.example.jira;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jira.adapter.api.JiraConnection;
import org.example.jira.adapter.api.JiraConnectionFactory;
import org.example.jira.adapter.api.JiraIssueRequest;
import org.example.jira.adapter.api.JiraIssueResult;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CamundaJiraIssueService {
    private final JiraAdapterProperties properties;
    private final ObjectProvider<JiraConnectionFactory> jiraConnectionFactoryProvider;

    public JiraIssueResult createFinancialOperationProblemIssue(
            Long orderId,
            Long paymentId,
            Long operationId,
            String operationStatus,
            String errorCode,
            String errorMessage
    ) {
        if (!properties.isEnabled()) {
            return JiraIssueResult.skipped("Jira integration disabled");
        }

        JiraConnectionFactory jiraConnectionFactory = jiraConnectionFactoryProvider.getIfAvailable();
        if (jiraConnectionFactory == null) {
            return JiraIssueResult.failed("Jira connection factory is unavailable");
        }

        JiraIssueRequest request = new JiraIssueRequest(
                paymentId,
                orderId,
                null,
                "Failed financial operation " + operationId + " for order " + orderId,
                createDescription(orderId, paymentId, operationId, operationStatus, errorCode, errorMessage),
                errorCode
        );

        try (JiraConnection connection = jiraConnectionFactory.getConnection()) {
            JiraIssueResult result = connection.createIssue(request);
            if (result.isCreated()) {
                log.info("Created Jira issue key={} for Camunda operationId={}", result.getIssueKey(), operationId);
            } else {
                log.warn("Jira issue was not created for Camunda operationId={}, reason={}", operationId, result.getMessage());
            }
            return result;
        } catch (Exception e) {
            log.warn("Jira issue creation failed for Camunda operationId={}", operationId, e);
            return JiraIssueResult.failed(e.getMessage());
        }
    }

    private String createDescription(
            Long orderId,
            Long paymentId,
            Long operationId,
            String operationStatus,
            String errorCode,
            String errorMessage
    ) {
        return """
                Financial operation processing failed.

                Source: Camunda purchase-process
                Order ID: %s
                Payment ID: %s
                Operation ID: %s
                Operation status: %s
                Error code: %s
                Error message: %s
                """.formatted(
                orderId,
                paymentId,
                operationId,
                operationStatus,
                errorCode,
                errorMessage
        );
    }
}
