package org.example.jira.adapter.spi;

import jakarta.resource.spi.ConnectionRequestInfo;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class JiraConnectionRequestInfo implements ConnectionRequestInfo, Serializable {
    private static final long serialVersionUID = 1L;
}
