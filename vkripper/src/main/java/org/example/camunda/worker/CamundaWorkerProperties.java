package org.example.camunda.worker;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.camunda.worker")
public class CamundaWorkerProperties {
    private boolean enabled = false;
    private String id = "vkripper-worker";
    private String validatePurchaseTopic = "validate-purchase";
    private String createBalancePurchaseTopic = "create-balance-purchase";
    private String publishFinancialOperationTopic = "publish-financial-operation";
    private String createJiraIssueTopic = "create-jira-issue";
    private String cleanupOldCartsTopic = "cleanup-old-carts";
    private long cleanupOldCartsOlderThanMinutes = 262800;
    private long lockDuration = 10000;
    private long pollDelay = 5000;
}
