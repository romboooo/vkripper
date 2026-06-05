package org.example.finance.jira;

import org.example.common.entity.FinancialOperation;
import org.example.jira.adapter.api.JiraIssueResult;

public interface JiraIssueService {

    JiraIssueResult createFinancialOperationProblemIssue(
            FinancialOperation operation,
            String errorCode,
            String errorMessage
    );
}
