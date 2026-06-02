package org.example.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = {"org.example.banking", "org.example.common"})
public class BankingNodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingNodeApplication.class, args);
    }
}
