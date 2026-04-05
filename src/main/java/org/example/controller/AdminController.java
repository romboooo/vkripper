package org.example.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.entity.Role;
import org.example.security.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "администрирование", description = "управление ролями и пользователями")
@RequiredArgsConstructor
public class AdminController {
    private final AuthService authService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(description = "назначить роль пользователю")
    @PutMapping("/users/{id}/roles")
    public ResponseEntity<String> assignRole(
            @Parameter(description = "ID пользователя")
            @PathVariable Long id,
            @Parameter(description = "роль: ROLE_SELLER, ROLE_BUYER, ROLE_MODERATOR, ROLE_ADMIN")
            @RequestParam Role role) {
        authService.assignRole(id, role);
        return ResponseEntity.ok("Роль " + role + "успешно назначена");
    }
}