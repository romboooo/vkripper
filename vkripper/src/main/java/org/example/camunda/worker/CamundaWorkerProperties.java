package org.example.camunda.worker;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.camunda.worker")
public class CamundaWorkerProperties {
    private boolean enabled = true;
    private String id = "vkripper-worker";
    private String validatePurchaseTopic = "validate-purchase";
    private String createBalancePurchaseTopic = "create-balance-purchase";
    private String publishFinancialOperationTopic = "publish-financial-operation";
    private String createJiraIssueTopic = "create-jira-issue";
    private String cleanupOldCartsTopic = "cleanup-old-carts";
    private String validateCartAddTopic = "validate-cart-add";
    private String executeCartAddTopic = "execute-cart-add";
    private String validateCartUpdateTopic = "validate-cart-update";
    private String executeCartUpdateTopic = "execute-cart-update";
    private String validateCartRemoveTopic = "validate-cart-remove";
    private String executeCartRemoveTopic = "execute-cart-remove";
    private String validateFavoriteAddTopic = "validate-favorite-add";
    private String executeFavoriteAddTopic = "execute-favorite-add";
    private String validateFavoriteRemoveTopic = "validate-favorite-remove";
    private String executeFavoriteRemoveTopic = "execute-favorite-remove";
    private String validateReviewCreateTopic = "validate-review-create";
    private String executeReviewCreateTopic = "execute-review-create";
    private String validateReviewUpdateTopic = "validate-review-update";
    private String executeReviewUpdateTopic = "execute-review-update";
    private String validateReviewDeleteTopic = "validate-review-delete";
    private String executeReviewDeleteTopic = "execute-review-delete";
    private String validateProductCreateTopic = "validate-product-create";
    private String executeProductCreateTopic = "execute-product-create";
    private String validateProductDeleteTopic = "validate-product-delete";
    private String executeProductDeleteTopic = "execute-product-delete";
    private long cleanupOldCartsOlderThanMinutes = 262800;
    private long lockDuration = 10000;
    private long pollDelay = 5000;
}
