package org.example.service;

import org.example.entity.User;

public interface UserService {
    User getUserEntityById(Long id);

    void saveUser(User user);
}
