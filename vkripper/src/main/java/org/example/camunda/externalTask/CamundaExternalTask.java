package org.example.camunda.externalTask;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.example.camunda.CamundaVariableValue;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CamundaExternalTask {
    private String id;
    private String topicName;
    private String workerId;
    private String processInstanceId;
    private String businessKey;
    private Map<String, CamundaVariableValue> variables;
}
