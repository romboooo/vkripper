package org.example.banking.jira;

public interface JiraIssueService {

    JiraIssueResult createPaymentProblemIssue(
            Long paymentId,
            Long orderId,
            Long userId,
            String errorCode,
            String errorMessage
    );
}