package org.example.banking;

import org.example.banking.jira.JiraProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@ConfigurationPropertiesScan
@EnableConfigurationProperties(JiraProperties.class)
@SpringBootApplication(scanBasePackages = "org.example")
public class BankingNodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingNodeApplication.class, args);
    }
}
