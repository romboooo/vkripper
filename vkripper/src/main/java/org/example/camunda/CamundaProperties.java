package org.example.camunda;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.camunda")
public class CamundaProperties {
    private String baseUrl = "http://localhost:18080/engine-rest";
    private String purchaseProcessKey = "purchase-process";
}
