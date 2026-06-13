package org.example.camunda;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CamundaVariableMapper {
    private CamundaVariableMapper() {
    }

    public static Map<String, Object> toCamundaVariables(Map<String, Object> variables) {
        Map<String, Object> camundaVariables = new LinkedHashMap<>();
        if (variables == null) {
            return camundaVariables;
        }
        variables.forEach((name, value) -> camundaVariables.put(name, toCamundaVariable(value)));
        return camundaVariables;
    }

    private static Map<String, Object> toCamundaVariable(Object value) {
        Map<String, Object> variable = new LinkedHashMap<>();
        variable.put("value", value);
        variable.put("type", camundaType(value));
        return variable;
    }

    private static String camundaType(Object value) {
        if (value instanceof String) {
            return "String";
        }
        if (value instanceof Long) {
            return "Long";
        }
        if (value instanceof Integer) {
            return "Integer";
        }
        if (value instanceof Boolean) {
            return "Boolean";
        }
        if (value instanceof Double || value instanceof BigDecimal) {
            return "Double";
        }
        throw new IllegalArgumentException(
                "Unsupported Camunda variable type: " + (value == null ? "null" : value.getClass().getName())
        );
    }
}
