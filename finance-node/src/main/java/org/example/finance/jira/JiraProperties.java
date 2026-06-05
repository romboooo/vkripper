package org.example.finance.jira;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jira")
@Data
public class JiraProperties {

    private boolean enabled;
    private String baseUrl;
    private String email;
    private String apiToken;
    private String projectKey;
    private String issueType;
}