package org.example.jira.adapter.spi;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConfigProperty;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterAssociation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.example.jira.adapter.api.JiraConnectionFactory;

import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = {
        "enabled",
        "baseUrl",
        "email",
        "apiToken",
        "projectKey",
        "issueType"
})
public class JiraManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation, Serializable {
    private static final long serialVersionUID = 1L;

    @ConfigProperty(defaultValue = "false")
    private Boolean enabled = Boolean.FALSE;

    @ConfigProperty
    private String baseUrl;

    @ConfigProperty
    private String email;

    @ConfigProperty(confidential = true)
    private String apiToken;

    @ConfigProperty
    private String projectKey;

    @ConfigProperty(defaultValue = "Task")
    private String issueType = "Task";

    private ResourceAdapter resourceAdapter;
    private PrintWriter logWriter;

    public boolean isEnabled() {
        if (enabled != null) {
            return Boolean.TRUE.equals(enabled);
        }
        return Boolean.parseBoolean(valueOrEnv(null, "APP_JIRA_ENABLED", "false"));
    }

    public String getBaseUrl() {
        return valueOrEnv(baseUrl, "APP_JIRA_BASE_URL");
    }

    public String getEmail() {
        return valueOrEnv(email, "APP_JIRA_EMAIL");
    }

    public String getApiToken() {
        return valueOrEnv(apiToken, "APP_JIRA_API_TOKEN");
    }

    public String getProjectKey() {
        return valueOrEnv(projectKey, "APP_JIRA_PROJECT_KEY");
    }

    public String getIssueType() {
        return valueOrEnv(issueType, "APP_JIRA_ISSUE_TYPE", "Task");
    }

    boolean hasRequiredSettings() {
        return hasText(getBaseUrl())
                && hasText(getEmail())
                && hasText(getApiToken())
                && hasText(getProjectKey())
                && hasText(getIssueType());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String valueOrEnv(String value, String envName) {
        return valueOrEnv(value, envName, null);
    }

    private String valueOrEnv(String value, String envName, String defaultValue) {
        if (hasText(value)) {
            return value;
        }
        String envValue = System.getenv(envName);
        if (hasText(envValue)) {
            return envValue;
        }
        return defaultValue;
    }

    @Override
    public Object createConnectionFactory(ConnectionManager connectionManager) {
        return new JiraConnectionFactoryImpl(this, connectionManager);
    }

    @Override
    public Object createConnectionFactory() {
        return new JiraConnectionFactoryImpl(this, new JiraConnectionManager());
    }

    @Override
    public ManagedConnection createManagedConnection(
            Subject subject,
            ConnectionRequestInfo connectionRequestInfo
    ) {
        return new JiraManagedConnection(this);
    }

    @Override
    public ManagedConnection matchManagedConnections(
            Set connectionSet,
            Subject subject,
            ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        for (Object connection : connectionSet) {
            if (connection instanceof JiraManagedConnection managedConnection) {
                return managedConnection;
            }
        }
        return null;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter resourceAdapter) {
        if (!(resourceAdapter instanceof JiraResourceAdapter)) {
            throw new IllegalArgumentException("Unsupported resource adapter: " + resourceAdapter);
        }
        this.resourceAdapter = resourceAdapter;
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }
}
