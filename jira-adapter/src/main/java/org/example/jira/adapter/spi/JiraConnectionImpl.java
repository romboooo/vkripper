package org.example.jira.adapter.spi;

import org.example.jira.adapter.api.JiraConnection;
import org.example.jira.adapter.api.JiraIssueRequest;
import org.example.jira.adapter.api.JiraIssueResult;

import java.io.Serializable;

public class JiraConnectionImpl implements JiraConnection {
    private static final long serialVersionUID = 1L;

    private JiraManagedConnection managedConnection;
    private boolean closed;

    public JiraConnectionImpl() {
    }

    public JiraConnectionImpl(JiraManagedConnection managedConnection) {
        this.managedConnection = managedConnection;
    }

    @Override
    public JiraIssueResult createIssue(JiraIssueRequest request) {
        if (closed || managedConnection == null) {
            throw new IllegalStateException("Jira connection is closed");
        }
        return managedConnection.createIssue(request);
    }

    @Override
    public void close() {
        if (!closed && managedConnection != null) {
            managedConnection.closeHandle(this);
        }
        closed = true;
        managedConnection = null;
    }

    void invalidate() {
        closed = true;
        managedConnection = null;
    }

    void associate(JiraManagedConnection managedConnection) {
        this.managedConnection = managedConnection;
        this.closed = false;
    }
}
