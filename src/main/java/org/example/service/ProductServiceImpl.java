package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.response.ProductListResponse;
import org.example.dto.response.ProductResponse;
import org.example.entity.Product;
import org.example.entity.ProductGroup;
import org.example.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;

    @Override
    @Secured({"BUYER", "SELLER", "MODERATOR", "ADMIN"})
    public ProductListResponse getCatalog(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> products = productRepository.findByAvailableTrue(pageable);
        return ProductListResponse.fromPage(products);
    }
    @Override
    @Secured({"BUYER", "SELLER", "MODERATOR", "ADMIN"})
    public List<ProductResponse> searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return productRepository
                .findByNameContainingIgnoreCaseAndAvailableTrue(keyword)
                .stream()
                .map(ProductResponse::fromProduct)
                .toList();
    }
    @Override
    @Secured({"BUYER", "SELLER", "MODERATOR", "ADMIN"})
    public ProductListResponse getProductsByGroup(ProductGroup group, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> products = productRepository.findByProductGroupAndAvailable(group, true, pageable);
        return ProductListResponse.fromPage(products);
    }
    @Override
    @Secured({"BUYER", "SELLER", "MODERATOR", "ADMIN"})
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));
        return ProductResponse.fromProduct(product);
    }
    @Override
    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар с id " + id + " не найден"));
    }
}