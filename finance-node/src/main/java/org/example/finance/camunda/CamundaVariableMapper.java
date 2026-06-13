package org.example.finance.camunda;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CamundaVariableMapper {
    private CamundaVariableMapper() {
    }

    public static Map<String, Object> variable(Object value, String type) {
        Map<String, Object> variable = new LinkedHashMap<>();
        variable.put("value", value);
        variable.put("type", type);
        return variable;
    }

    public static void putStringIfPresent(Map<String, Object> variables, String name, String value) {
        if (value != null && !value.isBlank()) {
            variables.put(name, variable(value, "String"));
        }
    }
}
