package org.example.finance.jca;

import org.example.finance.jira.JiraIssueRequest;
import org.example.finance.jira.JiraIssueResult;

public interface JiraConnection {

    JiraIssueResult createIssue(JiraIssueRequest request);
}