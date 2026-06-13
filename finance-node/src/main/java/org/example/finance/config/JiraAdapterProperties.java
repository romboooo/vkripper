package org.example.finance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.jira-adapter")
public class JiraAdapterProperties {
    private boolean enabled = false;
    private String connectionFactoryJndiName = "java:/eis/JiraConnectionFactory";
}
