package org.example.camunda.externalTask;

import org.example.camunda.CamundaVariableValue;

public final class CamundaExternalTaskVariables {
    private CamundaExternalTaskVariables() {
    }

    public static Long longVariable(CamundaExternalTask task, String name) {
        Object value = requiredValue(task, name);
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("Invalid Long variable: " + name);
    }

    public static Long optionalLongVariable(CamundaExternalTask task, String name) {
        if (task.getVariables() == null || !task.getVariables().containsKey(name)) {
            return null;
        }
        CamundaVariableValue variable = task.getVariables().get(name);
        if (variable == null || variable.getValue() == null) {
            return null;
        }
        Object value = variable.getValue();
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("Invalid Long variable: " + name);
    }

    public static Integer integerVariable(CamundaExternalTask task, String name) {
        Object value = requiredValue(task, name);
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalArgumentException("Invalid Integer variable: " + name);
    }

    public static String stringVariable(CamundaExternalTask task, String name) {
        Object value = requiredValue(task, name);
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return stringValue;
        }
        throw new IllegalArgumentException("Invalid String variable: " + name);
    }

    public static String optionalStringVariable(CamundaExternalTask task, String name) {
        if (task.getVariables() == null || !task.getVariables().containsKey(name)) {
            return null;
        }
        CamundaVariableValue variable = task.getVariables().get(name);
        if (variable == null || variable.getValue() == null) {
            return null;
        }
        Object value = variable.getValue();
        if (value instanceof String stringValue) {
            return stringValue;
        }
        throw new IllegalArgumentException("Invalid String variable: " + name);
    }

    private static Object requiredValue(CamundaExternalTask task, String name) {
        if (task.getVariables() == null || !task.getVariables().containsKey(name)) {
            throw new IllegalArgumentException("Missing Camunda variable: " + name);
        }
        CamundaVariableValue variable = task.getVariables().get(name);
        if (variable == null || variable.getValue() == null) {
            throw new IllegalArgumentException("Missing Camunda variable value: " + name);
        }
        return variable.getValue();
    }
}
