package org.example.service;

import org.example.dto.response.PurchaseProcessStartResponse;

import java.util.Map;

public interface CamundaBusinessProcessService {
    PurchaseProcessStartResponse startProcess(String processKey, String businessKeyPrefix, Map<String, Object> variables);
}
