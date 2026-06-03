package org.example.banking.jca;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.banking.jira.JiraProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JiraManagedConnectionFactory {

    private final JiraProperties jiraProperties;
    private final ObjectMapper objectMapper;

    public JiraConnection createManagedConnection() {
        return new JiraConnectionImpl(jiraProperties, objectMapper);
    }
}