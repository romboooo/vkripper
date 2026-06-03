package org.example.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = {"org.example.banking", "org.example.common"})
public class BankingNodeApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(BankingNodeApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(BankingNodeApplication.class, args);
    }
}
