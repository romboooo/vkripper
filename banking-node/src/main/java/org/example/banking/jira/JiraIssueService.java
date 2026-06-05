package org.example.banking.jira;

import org.example.jira.adapter.api.JiraIssueResult;

public interface JiraIssueService {

    JiraIssueResult createPaymentProblemIssue(
            Long paymentId,
            Long orderId,
            Long userId,
            String errorCode,
            String errorMessage
    );
}
