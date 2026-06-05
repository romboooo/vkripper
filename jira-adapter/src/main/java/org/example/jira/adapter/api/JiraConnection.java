package org.example.jira.adapter.api;

import java.io.Serializable;

public interface JiraConnection extends AutoCloseable, Serializable {

    JiraIssueResult createIssue(JiraIssueRequest request);

    @Override
    void close();
}
