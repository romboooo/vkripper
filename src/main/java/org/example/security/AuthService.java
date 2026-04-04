package org.example.security;

import org.example.dto.response.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthService extends UserDetailsService {
    UserResponse register(String username, String password);
    void assignRole(Long userId, String role);
    String login(String username, String password);

}