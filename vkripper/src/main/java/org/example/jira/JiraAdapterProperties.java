package org.example.jira;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.jira-adapter")
public class JiraAdapterProperties {
    private boolean enabled = true;
    private String connectionFactoryJndiName = "java:/eis/JiraConnectionFactory";
}
