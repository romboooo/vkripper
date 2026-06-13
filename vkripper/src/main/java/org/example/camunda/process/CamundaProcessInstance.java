package org.example.camunda.process;

public record CamundaProcessInstance(
        String id,
        String definitionId,
        String businessKey,
        String caseInstanceId,
        boolean ended,
        boolean suspended,
        String tenantId
) {
}
