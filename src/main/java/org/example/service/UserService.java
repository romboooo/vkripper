package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.response.UserResponse;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponse getById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("user is not found"));
        return UserResponse.fromUser(user);
    }

    protected User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с id " + id + " не найден"));
    }
    protected void saveUser(User user) {
        userRepository.save(user);
    }
}

