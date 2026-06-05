package org.example.jira.adapter.spi;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import org.example.jira.adapter.api.JiraConnection;
import org.example.jira.adapter.api.JiraConnectionFactory;

import javax.naming.Reference;
import java.io.Serializable;

public class JiraConnectionFactoryImpl implements JiraConnectionFactory, Serializable {
    private static final long serialVersionUID = 1L;

    private JiraManagedConnectionFactory managedConnectionFactory;
    private ConnectionManager connectionManager;
    private Reference reference;

    public JiraConnectionFactoryImpl() {
    }

    public JiraConnectionFactoryImpl(
            JiraManagedConnectionFactory managedConnectionFactory,
            ConnectionManager connectionManager
    ) {
        this.managedConnectionFactory = managedConnectionFactory;
        this.connectionManager = connectionManager;
    }

    @Override
    public JiraConnection getConnection() throws ResourceException {
        if (managedConnectionFactory == null || connectionManager == null) {
            throw new ResourceException("Jira connection factory is not initialized");
        }
        return (JiraConnection) connectionManager.allocateConnection(
                managedConnectionFactory,
                new JiraConnectionRequestInfo()
        );
    }

    @Override
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    @Override
    public Reference getReference() {
        return reference;
    }
}
