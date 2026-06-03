package org.example.banking.jca;

import org.example.banking.jira.JiraIssueRequest;
import org.example.banking.jira.JiraIssueResult;

public interface JiraConnection {

    JiraIssueResult createIssue(JiraIssueRequest request);
}