package org.example.finance.jca;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.finance.jira.JiraIssueRequest;
import org.example.finance.jira.JiraIssueResult;
import org.example.finance.jira.JiraProperties;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JiraConnectionImpl implements JiraConnection {

    private final JiraProperties jiraProperties;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public JiraIssueResult createIssue(JiraIssueRequest request) {
        if (!jiraProperties.isEnabled()) {
            log.info(
                    "Jira integration disabled. Mock issue creation: paymentId={}, orderId={}, summary={}",
                    request.getPaymentId(),
                    request.getOrderId(),
                    request.getSummary()
            );

            return JiraIssueResult.skipped("Jira integration disabled");
        }

        try {
            String url = jiraProperties.getBaseUrl() + "/rest/api/3/issue";

            Map<String, Object> body = Map.of(
                    "fields", Map.of(
                            "project", Map.of(
                                    "key", jiraProperties.getProjectKey()
                            ),
                            "summary", request.getSummary(),
                            "description", createAdfDescription(request.getDescription()),
                            "issuetype", Map.of(
                                    "name", jiraProperties.getIssueType()
                            )
                    )
            );

            String jsonBody = objectMapper.writeValueAsString(body);

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

                String issueKey = null;
                if (json.has("key")) {
                    issueKey = json.get("key").asText();
                }

                log.info("Jira issue created: issueKey={}", issueKey);

                return JiraIssueResult.created(issueKey);
            }

            log.warn(
                    "Failed to create Jira issue. status={}, body={}",
                    response.statusCode(),
                    response.body()
            );

            return JiraIssueResult.failed(
                    "Jira returned status " + response.statusCode()
            );
        } catch (Exception e) {
            log.error("Jira issue creation failed", e);
            return JiraIssueResult.failed(e.getMessage());
        }
    }

    private String createBasicAuthHeader() {
        String rawAuth = jiraProperties.getEmail() + ":" + jiraProperties.getApiToken();

        String encodedAuth = Base64.getEncoder().encodeToString(
                rawAuth.getBytes(StandardCharsets.UTF_8)
        );

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
                                                "text", text
                                        )
                                )
                        )
                )
        );
    }
}