package org.example.camunda.process;

public class CamundaProcessStartException extends RuntimeException {
    public CamundaProcessStartException(String message, Throwable cause) {
        super(message, cause);
    }
}
