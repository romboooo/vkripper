package org.example.banking.jira;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.banking.config.JiraAdapterProperties;
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
    public JiraIssueResult createPaymentProblemIssue(
            Long paymentId,
            Long orderId,
            Long userId,
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
                userId,
                "Payment processing problem: paymentId=" + paymentId,
                createDescription(paymentId, orderId, userId, errorCode, errorMessage),
                errorCode
        );

        try (JiraConnection connection = jiraConnectionFactory.getConnection()) {
            return connection.createIssue(request);
        } catch (Exception e) {
            log.warn("Jira issue creation failed for paymentId={}", paymentId, e);
            return JiraIssueResult.failed(e.getMessage());
        }
    }

    private String createDescription(
            Long paymentId,
            Long orderId,
            Long userId,
            String errorCode,
            String errorMessage
    ) {
        return """
                Payment processing failed.

                Payment ID: %s
                Order ID: %s
                User ID: %s
                Error code: %s
                Error message: %s
                """.formatted(paymentId, orderId, userId, errorCode, errorMessage);
    }
}
