package org.example.camunda.externalTask;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.camunda.CamundaProperties;
import org.example.camunda.CamundaVariableMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CamundaExternalTaskClient {
    private final CamundaProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public List<CamundaExternalTask> fetchAndLock(
            String workerId,
            String topicName,
            int maxTasks,
            long lockDuration
    ) {
        Map<String, Object> topic = new LinkedHashMap<>();
        topic.put("topicName", topicName);
        topic.put("lockDuration", lockDuration);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("workerId", workerId);
        requestBody.put("maxTasks", maxTasks);
        requestBody.put("usePriority", true);
        requestBody.put("topics", List.of(topic));

        HttpRequest request = jsonPost(uri("/external-task/fetchAndLock"), requestBody);
        HttpResponse<String> response = send(request, "fetch and lock Camunda external tasks");
        try {
            CamundaExternalTask[] tasks = objectMapper.readValue(response.body(), CamundaExternalTask[].class);
            return Arrays.asList(tasks);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse Camunda external task response", e);
        }
    }

    public void complete(String taskId, String workerId, Map<String, Object> variables) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("workerId", workerId);
        requestBody.put("variables", CamundaVariableMapper.toCamundaVariables(variables));

        HttpRequest request = jsonPost(uri("/external-task/" + path(taskId) + "/complete"), requestBody);
        send(request, "complete Camunda external task");
    }

    public void handleFailure(String taskId, String workerId, String errorMessage, String errorDetails) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("workerId", workerId);
        requestBody.put("errorMessage", errorMessage);
        requestBody.put("errorDetails", errorDetails);
        requestBody.put("retries", 0);

        HttpRequest request = jsonPost(uri("/external-task/" + path(taskId) + "/failure"), requestBody);
        send(request, "mark Camunda external task as failed");
    }

    private HttpRequest jsonPost(URI uri, Map<String, Object> requestBody) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(writeJson(requestBody)))
                .build();
    }

    private HttpResponse<String> send(HttpRequest request, String action) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Failed to %s. status=%s, body=%s"
                                .formatted(action, response.statusCode(), response.body())
                );
            }
            return response;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call Camunda REST API to " + action, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Camunda REST API call was interrupted while trying to " + action, e);
        }
    }

    private URI uri(String path) {
        return URI.create(properties.getBaseUrl().replaceAll("/+$", "") + path);
    }

    private String path(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String writeJson(Map<String, Object> requestBody) {
        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize Camunda external task request", e);
        }
    }
}
