package org.example.jira.adapter.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssueResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean created;
    private String issueKey;
    private String message;

    public static JiraIssueResult created(String issueKey) {
        return new JiraIssueResult(true, issueKey, "Jira issue created");
    }

    public static JiraIssueResult skipped(String message) {
        return new JiraIssueResult(false, null, message);
    }

    public static JiraIssueResult failed(String message) {
        return new JiraIssueResult(false, null, message);
    }
}
