package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.UserRequest;
import org.example.dto.response.UserResponse;
import org.example.security.CustomUserDetails;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @PostMapping
    @Operation(summary = "пополнить баланс", description = "пополняет баланс конкретного покупателя")
    public ResponseEntity<UserResponse> addMoney(
            @Valid @RequestBody UserRequest userRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser){
        return ResponseEntity.ok(userService.addMoney(currentUser.getId(), userRequest.getAmount()));
    }


    @PostMapping
    @Operation(summary = "вывести деньги", description = "выводит деньги со счета конкретного покупателя")
    public ResponseEntity<UserResponse> withdrawMoney(
            @Valid @RequestBody UserRequest userRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser){
        return ResponseEntity.ok(userService.witdrawMoney(currentUser.getId(), userRequest.getAmount()));
    }

}
