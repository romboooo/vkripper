package org.example.service;


import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.dto.response.ShoppingCartResponse;
import org.example.repository.ShoppingCartRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;

    public List<ShoppingCartResponse> getByUserId(Long userId){
        List<ShoppingCartResponse> response = shoppingCartRepository.findByUserId(userId)
                .stream()
                .map(ShoppingCartResponse::fromShoppingCart)
                .toList();

        return response;
    }
}
