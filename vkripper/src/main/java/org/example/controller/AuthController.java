package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.UserRequest;
import org.example.dto.response.UserResponse;
import org.example.security.AuthService;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name="аутентификация", description = "вход и выход из аккаунтов")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Создает новый аккаунт с ролью ПОКУПАТЕЛЬ по умолчанию")
    public ResponseEntity<UserResponse> register(
            @Parameter(description = "Данные для регистрации (логин и пароль)")
            @RequestBody @Valid UserRequest request) {

        UserResponse user = authService.register(request.getUsername(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Возвращает JWT токен при успешной аутентификации")
    public ResponseEntity<Map<String, String>> login(
            @Parameter(description = "Данные для входа")
            @RequestBody @Valid UserRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }


}