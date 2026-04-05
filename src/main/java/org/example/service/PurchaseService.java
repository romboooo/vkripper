package org.example.service;

import org.example.dto.request.PurchaseRequest;
import org.example.dto.response.PurchaseResponse;

public interface PurchaseService {
    PurchaseResponse createPurchase(Long userId, PurchaseRequest request);
}
