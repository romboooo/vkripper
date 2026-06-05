package org.example.jira.adapter.spi;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;

import javax.transaction.xa.XAResource;
import java.util.Objects;

public class JiraResourceAdapter implements ResourceAdapter {

    @Override
    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
    }

    @Override
    public void stop() {
    }

    @Override
    public void endpointActivation(
            MessageEndpointFactory endpointFactory,
            ActivationSpec spec
    ) throws ResourceException {
        throw new ResourceException("Jira adapter does not support inbound messaging");
    }

    @Override
    public void endpointDeactivation(
            MessageEndpointFactory endpointFactory,
            ActivationSpec spec
    ) {
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return new XAResource[0];
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof JiraResourceAdapter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(JiraResourceAdapter.class);
    }
}
