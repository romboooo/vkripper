package org.example.finance.jira;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.entity.FinancialOperation;
import org.example.finance.config.JiraAdapterProperties;
import org.example.jira.adapter.api.JiraConnection;
import org.example.jira.adapter.api.JiraConnectionFactory;
import org.example.jira.adapter.api.JiraIssueRequest;
import org.example.jira.adapter.api.JiraIssueResult;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraIssueServiceImpl implements JiraIssueService {
    private final JiraAdapterProperties properties;
    private final ObjectProvider<JiraConnectionFactory> jiraConnectionFactoryProvider;

    @Override
    public JiraIssueResult createFinancialOperationProblemIssue(
            FinancialOperation operation,
            String errorCode,
            String errorMessage
    ) {
        if (!properties.isEnabled()) {
            log.warn("Jira issue was not created for operationId={}, reason=Jira integration disabled", operation.getId());
            return JiraIssueResult.skipped("Jira integration disabled");
        }

        JiraConnectionFactory jiraConnectionFactory = jiraConnectionFactoryProvider.getIfAvailable();
        if (jiraConnectionFactory == null) {
            log.warn("Jira issue was not created for operationId={}, reason=Jira connection factory is unavailable", operation.getId());
            return JiraIssueResult.failed("Jira connection factory is unavailable");
        }

        JiraIssueRequest request = new JiraIssueRequest(
                operation.getPaymentId(),
                operation.getOrderId(),
                operation.getUserId(),
                "Financial operation problem: operationId=" + operation.getId(),
                createDescription(operation, errorCode, errorMessage),
                errorCode
        );

        try (JiraConnection connection = jiraConnectionFactory.getConnection()) {
            JiraIssueResult result = connection.createIssue(request);
            if (result.isCreated()) {
                log.info("Created Jira issue key={} for operationId={}", result.getIssueKey(), operation.getId());
            } else {
                log.warn("Jira issue was not created for operationId={}, reason={}", operation.getId(), result.getMessage());
            }
            return result;
        } catch (Exception e) {
            log.warn("Jira issue creation failed for operationId={}", operation.getId(), e);
            return JiraIssueResult.failed(e.getMessage());
        }
    }

    private String createDescription(
            FinancialOperation operation,
            String errorCode,
            String errorMessage
    ) {
        return """
                Financial operation processing failed.

                Operation ID: %s
                Operation type: %s
                Payment ID: %s
                Order ID: %s
                User ID: %s
                Error code: %s
                Error message: %s
                """.formatted(
                operation.getId(),
                operation.getType(),
                operation.getPaymentId(),
                operation.getOrderId(),
                operation.getUserId(),
                errorCode,
                errorMessage
        );
    }
}
