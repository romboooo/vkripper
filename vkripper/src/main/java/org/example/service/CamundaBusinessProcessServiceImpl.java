package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.camunda.CamundaProperties;
import org.example.camunda.process.CamundaProcessClient;
import org.example.camunda.process.CamundaProcessInstance;
import org.example.camunda.process.CamundaProcessStartException;
import org.example.dto.response.PurchaseProcessStartResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CamundaBusinessProcessServiceImpl implements CamundaBusinessProcessService {
    private static final String PROCESS_STARTED = "PROCESS_STARTED";

    private final CamundaProperties camundaProperties;
    private final CamundaProcessClient camundaProcessClient;

    @Override
    public PurchaseProcessStartResponse startProcess(String processKey, String businessKeyPrefix, Map<String, Object> variables) {
        String businessKey = businessKeyPrefix + "-" + Instant.now().toEpochMilli();
        log.info(
                "Starting Camunda business process processKey={}, businessKey={}, variables={}",
                processKey,
                businessKey,
                variables.keySet()
        );

        try {
            CamundaProcessInstance processInstance = camundaProcessClient.startProcess(processKey, businessKey, variables);
            return new PurchaseProcessStartResponse(PROCESS_STARTED, businessKey, processInstance.id());
        } catch (RuntimeException e) {
            throw new CamundaProcessStartException(
                    "Failed to start Camunda business process. Check that Camunda standalone is available at "
                            + camundaProperties.getBaseUrl(),
                    e
            );
        }
    }
}
