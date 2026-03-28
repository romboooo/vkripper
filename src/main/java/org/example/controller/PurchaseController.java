package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.PurchaseRequest;
import org.example.dto.response.PurchaseResponse;
import org.example.service.PurchaseServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/purchases")
@Tag(name = "Покупки", description = "Управление покупками товаров")
public class PurchaseController {

    private final PurchaseServiceImpl purchaseService;

    @PostMapping
    @Operation(summary = "Оформить покупку", description = "Создаёт новую покупку выбранного типа")
    public ResponseEntity<PurchaseResponse> createPurchase(
            @Parameter(description = "Данные для оформления покупки")
            @Valid @RequestBody PurchaseRequest request) {
        return ResponseEntity.ok(purchaseService.createPurchase(request));
    }
}