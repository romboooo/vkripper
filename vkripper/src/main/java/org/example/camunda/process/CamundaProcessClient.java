package org.example.camunda.process;

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
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CamundaProcessClient {
    private final CamundaProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public CamundaProcessInstance startProcess(
            String processKey,
            String businessKey,
            Map<String, Object> variables
    ) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        if (businessKey != null && !businessKey.isBlank()) {
            requestBody.put("businessKey", businessKey);
        }
        requestBody.put("variables", CamundaVariableMapper.toCamundaVariables(variables));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(startProcessUri(processKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(writeJson(requestBody)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Failed to start Camunda process. status=%s, body=%s"
                                .formatted(response.statusCode(), response.body())
                );
            }
            return objectMapper.readValue(response.body(), CamundaProcessInstance.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call Camunda REST API", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Camunda REST API call was interrupted", e);
        }
    }

    private URI startProcessUri(String processKey) {
        String normalizedBaseUrl = properties.getBaseUrl().replaceAll("/+$", "");
        String encodedProcessKey = URLEncoder.encode(processKey, StandardCharsets.UTF_8);
        return URI.create(normalizedBaseUrl + "/process-definition/key/" + encodedProcessKey + "/start");
    }

    private String writeJson(Map<String, Object> requestBody) {
        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize Camunda start process request", e);
        }
    }
}
