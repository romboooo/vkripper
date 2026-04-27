package org.example.config;

import org.example.repository.UserRepository;
import org.example.security.AppLoginModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class JaasConfig {

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void initJaas() {
        AppLoginModule.setUserRepository(userRepository);
    }
}