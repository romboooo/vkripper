package org.example.finance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.finance")
public class FinanceProcessingProperties {
    private Duration processingDelay = Duration.ofSeconds(3);

    public Duration getProcessingDelay() {
        return processingDelay;
    }

    public void setProcessingDelay(Duration processingDelay) {
        this.processingDelay = processingDelay;
    }
}
