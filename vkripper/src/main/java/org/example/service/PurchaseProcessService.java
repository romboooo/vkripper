package org.example.service;

import org.example.dto.request.PurchaseRequest;
import org.example.dto.response.PurchaseProcessStartResponse;

public interface PurchaseProcessService {
    PurchaseProcessStartResponse startPurchaseProcess(Long buyerId, PurchaseRequest request);
}
