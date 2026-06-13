package org.example.camunda.externalTask;

import org.example.camunda.CamundaVariableValue;

import java.math.BigDecimal;

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

    public static Boolean booleanVariable(CamundaExternalTask task, String name) {
        Object value = requiredValue(task, name);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        throw new IllegalArgumentException("Invalid Boolean variable: " + name);
    }

    public static BigDecimal bigDecimalVariable(CamundaExternalTask task, String name) {
        Object value = requiredValue(task, name);
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return new BigDecimal(stringValue);
        }
        throw new IllegalArgumentException("Invalid BigDecimal variable: " + name);
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
