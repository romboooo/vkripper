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
}

