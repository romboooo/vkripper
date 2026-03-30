package org.example.service;

import org.example.dto.response.UserResponse;
import org.example.entity.User;

public interface UserService {
    UserResponse getById(Long id);

    User getUserEntityById(Long id);

    void saveUser(User user);
}
