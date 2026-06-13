package org.example.camunda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CamundaVariableValue {
    private Object value;
    private String type;
    private Map<String, Object> valueInfo;
}
