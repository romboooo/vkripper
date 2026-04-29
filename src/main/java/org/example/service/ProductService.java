package org.example.service;

import jakarta.validation.Valid;
import org.example.dto.request.ProductRequest;
import org.example.dto.response.ProductListResponse;
import org.example.dto.response.ProductResponse;
import org.example.entity.Product;
import org.example.entity.ProductGroup;

import java.util.List;

public interface ProductService {

    ProductListResponse getCatalog(int page, int size);

    List<ProductResponse> searchProducts(String keyword);

    ProductListResponse getProductsByGroup(ProductGroup group, int page, int size);

    ProductResponse getProductById(Long id);

    Product getProductEntityById(Long id);

    ProductResponse createProduct(Long id, ProductRequest request);

    void deleteProduct(Long userId, Long productId);
}

