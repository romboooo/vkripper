package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.MoneyRequest;
import org.example.dto.request.TopUpRequest;
import org.example.dto.request.UserRequest;
import org.example.dto.request.WithdrawRequest;
import org.example.dto.response.UserResponse;
import org.example.security.CustomUserDetails;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/addMoney")
    @Operation(summary = "пополнить баланс", description = "пополняет баланс конкретного покупателя")
    public ResponseEntity<UserResponse> addMoney(
            @Valid @RequestBody TopUpRequest topUpRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser){
        return ResponseEntity.ok(userService.addMoney(currentUser.getId(), topUpRequest.getMoneyAmount()));
    }


    @PostMapping("/withdrawMoney")
    @Operation(summary = "вывести деньги", description = "выводит деньги со счета конкретного покупателя")
    public ResponseEntity<UserResponse> withdrawMoney(
            @Valid @RequestBody WithdrawRequest withdrawRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser){
        return ResponseEntity.ok(userService.witdrawMoney(currentUser.getId(),withdrawRequest.getMoneyAmount()));
    }

    @PostMapping("/unban")
    @Operation(summary = "забанить пользователя", description = "дает возможность администратору банить пользователя")
    public ResponseEntity<UserResponse> banUser(
        @Valid @Parameter(description = "ID пользователя")Long userId
    ){
        return ResponseEntity.ok(userService.banUser(userId));
    }

    @PostMapping("/ban")
    @Operation(summary = "разбанить пользователя", description = "дает возможность администратору разбанить пользователя")
    public ResponseEntity<UserResponse> unbanUser(
            @Valid @Parameter(description = "ID пользователя")Long userId
    ){
        return ResponseEntity.ok(userService.unbanUser(userId));
    }


}
