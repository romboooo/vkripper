package org.example.finance.jira;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.finance.jca.JiraConnection;
import org.example.finance.jca.JiraConnectionFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraIssueServiceImpl implements JiraIssueService {

    private final JiraConnectionFactory jiraConnectionFactory;

    @Override
    public JiraIssueResult createPaymentProblemIssue(
            Long paymentId,
            Long orderId,
            Long userId,
            String errorCode,
            String errorMessage
    ) {
        String summary = "Payment processing problem: paymentId=" + paymentId;

        String description = """
                Payment processing failed.

                Payment ID: %s
                Order ID: %s
                User ID: %s
                Error code: %s
                Error message: %s

                This issue was created automatically by finance-node through JCA adapter.
                """.formatted(
                paymentId,
                orderId,
                userId,
                errorCode,
                errorMessage
        );

        JiraIssueRequest request = new JiraIssueRequest(
                paymentId,
                orderId,
                userId,
                summary,
                description,
                errorCode
        );

        JiraConnection connection = jiraConnectionFactory.getConnection();
        JiraIssueResult result = connection.createIssue(request);

        log.info(
                "Jira issue service result: created={}, issueKey={}, message={}",
                result.isCreated(),
                result.getIssueKey(),
                result.getMessage()
        );

        return result;
    }
}