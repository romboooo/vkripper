package org.example.finance.camunda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CamundaMessageClient {
    private final CamundaProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void correlateFinancialOperationFinished(
            Long operationId,
            String operationStatus,
            String errorCode,
            String errorMessage
    ) {
        Map<String, Object> correlationKeys = new LinkedHashMap<>();
        correlationKeys.put("operationId", CamundaVariableMapper.variable(operationId, "Long"));

        Map<String, Object> processVariables = new LinkedHashMap<>();
        processVariables.put("operationStatus", CamundaVariableMapper.variable(operationStatus, "String"));
        CamundaVariableMapper.putStringIfPresent(processVariables, "errorCode", errorCode);
        CamundaVariableMapper.putStringIfPresent(processVariables, "errorMessage", errorMessage);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("messageName", properties.getFinancialOperationFinishedMessage());
        requestBody.put("correlationKeys", correlationKeys);
        requestBody.put("processVariables", processVariables);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(messageUri())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(writeJson(requestBody)))
                .build();

        send(request);
    }

    private URI messageUri() {
        return URI.create(properties.getBaseUrl().replaceAll("/+$", "") + "/message");
    }

    private void send(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Failed to correlate Camunda message. status=%s, body=%s"
                                .formatted(response.statusCode(), response.body())
                );
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call Camunda REST API for message correlation", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Camunda message correlation was interrupted", e);
        }
    }

    private String writeJson(Map<String, Object> requestBody) {
        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize Camunda message correlation request", e);
        }
    }
}
