package org.example.jira.adapter.spi;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;

import javax.security.auth.Subject;
import java.io.Serializable;

public class JiraConnectionManager implements ConnectionManager, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public Object allocateConnection(
            ManagedConnectionFactory managedConnectionFactory,
            ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        ManagedConnection managedConnection = managedConnectionFactory.createManagedConnection(
                new Subject(),
                connectionRequestInfo
        );
        return managedConnection.getConnection(new Subject(), connectionRequestInfo);
    }
}
