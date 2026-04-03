package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.response.UserResponse;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public UserResponse getById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("user is not found"));
        return UserResponse.fromUser(user);
    }


    @Override
    public User getUserEntityByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Пользователь с именем " + username + " не найден");
        }
        return user;
    }
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void saveUser(User user) {
        userRepository.save(user);
    }
}

