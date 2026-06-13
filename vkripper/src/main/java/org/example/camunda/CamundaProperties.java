package org.example.camunda;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.camunda")
public class CamundaProperties {
    private String baseUrl = "http://localhost:18080/engine-rest";
    private String purchaseProcessKey = "purchase-process";
    private String cartAddProcessKey = "cart-add-process";
    private String cartUpdateProcessKey = "cart-update-process";
    private String cartRemoveProcessKey = "cart-remove-process";
    private String favoriteAddProcessKey = "favorite-add-process";
    private String favoriteRemoveProcessKey = "favorite-remove-process";
    private String reviewCreateProcessKey = "review-create-process";
    private String reviewUpdateProcessKey = "review-update-process";
    private String reviewDeleteProcessKey = "review-delete-process";
    private String productCreateProcessKey = "product-create-process";
    private String productDeleteProcessKey = "product-delete-process";
}
