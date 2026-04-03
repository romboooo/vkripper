package org.example.service;

import org.example.dto.response.UserResponse;
import org.example.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthService extends UserDetailsService {
    UserResponse register(String username, String password);
    void assignRole(Long userId, String role);

}
