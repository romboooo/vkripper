package org.example.jira.adapter.spi;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnectionMetaData;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JiraConnectionMetaData implements ManagedConnectionMetaData {
    private final String userName;

    @Override
    public String getEISProductName() throws ResourceException {
        return "Jira REST API";
    }

    @Override
    public String getEISProductVersion() throws ResourceException {
        return "3";
    }

    @Override
    public int getMaxConnections() throws ResourceException {
        return 0;
    }

    @Override
    public String getUserName() throws ResourceException {
        return userName;
    }
}
