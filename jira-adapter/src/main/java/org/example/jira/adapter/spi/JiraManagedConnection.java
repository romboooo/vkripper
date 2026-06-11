package org.example.jira.adapter.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.LocalTransaction;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionMetaData;
import lombok.RequiredArgsConstructor;
import org.example.jira.adapter.api.JiraIssueRequest;
import org.example.jira.adapter.api.JiraIssueResult;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class JiraManagedConnection implements ManagedConnection {
    private final JiraManagedConnectionFactory factory;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final List<ConnectionEventListener> listeners = new ArrayList<>();
    private final Set<JiraConnectionImpl> handles = new HashSet<>();

    private PrintWriter logWriter;

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) {
        JiraConnectionImpl handle = new JiraConnectionImpl(this);
        handles.add(handle);
        return handle;
    }

    @Override
    public void destroy() {
        cleanup();
    }

    @Override
    public void cleanup() {
        for (JiraConnectionImpl handle : new HashSet<>(handles)) {
            handle.invalidate();
        }
        handles.clear();
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        if (!(connection instanceof JiraConnectionImpl jiraConnection)) {
            throw new ResourceException("Unsupported connection handle: " + connection);
        }
        jiraConnection.associate(this);
        handles.add(jiraConnection);
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        throw new NotSupportedException("Jira adapter does not support XA transactions");
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        throw new NotSupportedException("Jira adapter does not support local transactions");
    }

    @Override
    public ManagedConnectionMetaData getMetaData() {
        return new JiraConnectionMetaData(factory.getEmail());
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() {
        return logWriter;
    }

    JiraIssueResult createIssue(JiraIssueRequest request) {
        if (!factory.isEnabled()) {
            return JiraIssueResult.skipped("Jira integration disabled");
        }

        if (!factory.hasRequiredSettings()) {
            return JiraIssueResult.failed("Jira connection settings are incomplete");
        }

        try {
            String url = factory.getBaseUrl() + "/rest/api/3/issue";
            String jsonBody = objectMapper.writeValueAsString(Map.of(
                    "fields", Map.of(
                            "project", Map.of("key", factory.getProjectKey()),
                            "summary", request.getSummary(),
                            "description", createAdfDescription(request.getDescription()),
                            "issuetype", Map.of("name", factory.getIssueType())
                    )
            ));

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", createBasicAuthHeader())
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode json = objectMapper.readTree(response.body());
                String issueKey = json.has("key") ? json.get("key").asText() : null;
                return JiraIssueResult.created(issueKey);
            }

            return JiraIssueResult.failed("Jira returned status " + response.statusCode());
        } catch (Exception e) {
            return JiraIssueResult.failed(e.getMessage());
        }
    }

    void closeHandle(JiraConnectionImpl handle) {
        handles.remove(handle);
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
        event.setConnectionHandle(handle);
        for (ConnectionEventListener listener : new ArrayList<>(listeners)) {
            listener.connectionClosed(event);
        }
    }

    private String createBasicAuthHeader() {
        String rawAuth = factory.getEmail() + ":" + factory.getApiToken();
        String encodedAuth = Base64.getEncoder().encodeToString(rawAuth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedAuth;
    }

    private Map<String, Object> createAdfDescription(String text) {
        return Map.of(
                "type", "doc",
                "version", 1,
                "content", List.of(
                        Map.of(
                                "type", "paragraph",
                                "content", List.of(
                                        Map.of(
                                                "type", "text",
                                                "text", text == null ? "" : text
                                        )
                                )
                        )
                )
        );
    }
}
