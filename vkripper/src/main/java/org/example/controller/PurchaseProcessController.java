package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.PurchaseRequest;
import org.example.dto.response.PurchaseProcessStartResponse;
import org.example.security.CustomUserDetails;
import org.example.service.PurchaseProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/purchases/process")
@Tag(name = "Покупки", description = "Управление Camunda-процессом покупки")
public class PurchaseProcessController {
    private final PurchaseProcessService purchaseProcessService;

    @PostMapping
    @Operation(summary = "Запустить процесс покупки", description = "Стартует BPMN-процесс покупки в Camunda")
    public ResponseEntity<PurchaseProcessStartResponse> startPurchaseProcess(
            @Parameter(description = "Данные для запуска процесса покупки")
            @Valid @RequestBody PurchaseRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(purchaseProcessService.startPurchaseProcess(currentUser.getId(), request));
    }
}
