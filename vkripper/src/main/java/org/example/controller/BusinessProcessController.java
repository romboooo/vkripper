package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.camunda.CamundaProperties;
import org.example.dto.request.FavoriteRequest;
import org.example.dto.request.ProductRequest;
import org.example.dto.request.ReviewRequest;
import org.example.dto.request.ShoppingCartRequest;
import org.example.dto.response.PurchaseProcessStartResponse;
import org.example.security.CustomUserDetails;
import org.example.service.CamundaBusinessProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BusinessProcessController {
    private final CamundaProperties camundaProperties;
    private final CamundaBusinessProcessService businessProcessService;

    @PostMapping("/api/shoppingCart/process/add")
    @PreAuthorize("hasAuthority('BUYER')")
    public ResponseEntity<PurchaseProcessStartResponse> startCartAddProcess(
            @RequestBody ShoppingCartRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Map<String, Object> variables = userVariables(currentUser);
        variables.put("productId", request.getProductId());
        return ResponseEntity.ok(businessProcessService.startProcess(
                camundaProperties.getCartAddProcessKey(),
                "cart-add-%s-%s".formatted(currentUser.getId(), request.getProductId()),
                variables
        ));
    }

    @PutMapping("/api/shoppingCart/process/update")
    @PreAuthorize("hasAuthority('BUYER')")
    public ResponseEntity<PurchaseProcessStartResponse> startCartUpdateProcess(
            @RequestParam Long productId,
            @RequestParam int newAmount,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Map<String, Object> variables = userVariables(currentUser);
        variables.put("productId", productId);
        variables.put("amount", newAmount);
        return ResponseEntity.ok(businessProcessService.startProcess(
                camundaProperties.getCartUpdateProcessKey(),
                "cart-update-%s-%s".formatted(currentUser.getId(), productId),
                variables
        ));
    }

    @DeleteMapping("/api/shoppingCart/process/remove/{productId}")
    @PreAuthorize("hasAuthority('BUYER')")
    public ResponseEntity<PurchaseProcessStartResponse> startCartRemoveProcess(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Map<String, Object> variables = userVariables(currentUser);
        variables.put("productId", productId);
        return ResponseEntity.ok(businessProcessService.startProcess(
                camundaProperties.getCartRemoveProcessKey(),
                "cart-remove-%s-%s".formatted(currentUser.getId(), productId),
                variables
        ));
    }

    @PostMapping("/api/favorite/process/add")
    @PreAuthorize("hasAuthority('BUYER')")
    public ResponseEntity<PurchaseProcessStartResponse> startFavoriteAddProcess(
            @RequestBody FavoriteRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Map<String, Object> variables = userVariables(currentUser);
        variables.put("productId", request.getProductId());
        return ResponseEntity.ok(businessProcessService.startProcess(
                camundaProperties.getFavoriteAddProcessKey(),
                "favorite-add-%s-%s".formatted(currentUser.getId(), request.getProductId()),
                variables
        ));
    }

    @DeleteMapping("/api/favorite/process/remove/{productId}")
    @PreAuthorize("hasAuthority('BUYER') or hasAuthority('MODERATOR') or hasAuthority('ADMIN')")
    public ResponseEntity<PurchaseProcessStartResponse> startFavoriteRemoveProcess(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Map<String, Object> variables = userVariables(currentUser);
        variables.put("productId", productId);
        return ResponseEntity.ok(businessProcessService.startProcess(
                camundaProperties.getFavoriteRemoveProcessKey(),
                "favorite-remove-%s-%s".formatted(currentUser.getId(), productId),
                variables
        ));
    }

    @PostMapping("/api/reviews/process/create")
    @PreAuthorize("hasAuthority('BUYER') or hasAuthority('SELLER')")
    public ResponseEntity<PurchaseProcessStartResponse> startReviewCreateProcess(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Map<String, Object> variables = reviewVariables(currentUser, request);
        return ResponseEntity.ok(businessProcessService.startProcess(
                camundaProperties.getReviewCreateProcessKey(),
                "review-create-%s-%s".formatted(currentUser.getId(), request.getProductId()),
                variables
        ));
    }

    @PutMapping("/api/reviews/process/update/{id}")
    @PreAuthorize("hasAuthority('BUYER') or hasAuthority('SELLER')")
    public ResponseEntity<PurchaseProcessStartResponse> startReviewUpdateProcess(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Map<String, Object> variables = reviewVariables(currentUser, request);
        variables.put("reviewId", id);
        return ResponseEntity.ok(businessProcessService.startProcess(
                camundaProperties.getReviewUpdateProcessKey(),
                "review-update-%s-%s".formatted(currentUser.getId(), id),
                variables
        ));
    }

    @DeleteMapping("/api/reviews/process/delete/{id}")
    @PreAuthorize("hasAuthority('BUYER') or hasAuthority('MODERATOR') or hasAuthority('ADMIN')")
    public ResponseEntity<PurchaseProcessStartResponse> startReviewDeleteProcess(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Map<String, Object> variables = userVariables(currentUser);
        variables.put("reviewId", id);
        return ResponseEntity.ok(businessProcessService.startProcess(
                camundaProperties.getReviewDeleteProcessKey(),
                "review-delete-%s-%s".formatted(currentUser.getId(), id),
                variables
        ));
    }

    @PostMapping("/api/products/process/create")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<PurchaseProcessStartResponse> startProductCreateProcess(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Map<String, Object> variables = userVariables(currentUser);
        variables.put("name", request.getName());
        variables.put("price", request.getPrice());
        variables.put("available", request.isAvailable());
        variables.put("productGroup", request.getProductGroup().name());
        return ResponseEntity.ok(businessProcessService.startProcess(
                camundaProperties.getProductCreateProcessKey(),
                "product-create-%s".formatted(currentUser.getId()),
                variables
        ));
    }

    @DeleteMapping("/api/products/process/delete/{productId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MODERATOR') or hasAuthority('SELLER')")
    public ResponseEntity<PurchaseProcessStartResponse> startProductDeleteProcess(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Map<String, Object> variables = userVariables(currentUser);
        variables.put("productId", productId);
        return ResponseEntity.ok(businessProcessService.startProcess(
                camundaProperties.getProductDeleteProcessKey(),
                "product-delete-%s-%s".formatted(currentUser.getId(), productId),
                variables
        ));
    }

    private Map<String, Object> reviewVariables(CustomUserDetails currentUser, ReviewRequest request) {
        Map<String, Object> variables = userVariables(currentUser);
        if (request.getProductId() != null) {
            variables.put("productId", request.getProductId());
        }
        variables.put("rating", request.getRating());
        variables.put("text", request.getText());
        return variables;
    }

    private Map<String, Object> userVariables(CustomUserDetails currentUser) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("userId", currentUser.getId());
        variables.put("role", role(currentUser));
        return variables;
    }

    private String role(CustomUserDetails currentUser) {
        return currentUser.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
    }
}
