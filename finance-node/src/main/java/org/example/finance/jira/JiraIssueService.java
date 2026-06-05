package org.example.finance.jira;

public interface JiraIssueService {

    JiraIssueResult createPaymentProblemIssue(
            Long paymentId,
            Long orderId,
            Long userId,
            String errorCode,
            String errorMessage
    );
}