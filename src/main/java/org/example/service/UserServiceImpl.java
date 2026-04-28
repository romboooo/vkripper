package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.response.UserResponse;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    @Override
    @Secured("BUYER")
    public UserResponse addMoney(Long id, BigDecimal amount){
        User user = getUserEntityById(id);
        user.setBalance(user.getBalance().add(amount));
        saveUser(user);
        return UserResponse.fromUser(user);
    }

    @Secured({"BUYER", "SELLER"})
    @Override
    public UserResponse witdrawMoney(Long id, BigDecimal amount){
        User user = getUserEntityById(id);
        if(user.getBalance().compareTo(amount) < 0){
            throw new IllegalArgumentException("недостаточно средств на счете");
        }
        user.setBalance(user.getBalance().subtract(amount));
        saveUser(user);
        return UserResponse.fromUser(user);
    }

    @Override
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с id " + id + " не найден"));
    }
    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }
}

