package org.example.jira.adapter.api;

import jakarta.resource.Referenceable;
import jakarta.resource.ResourceException;

import java.io.Serializable;

public interface JiraConnectionFactory extends Serializable, Referenceable {

    JiraConnection getConnection() throws ResourceException;
}
