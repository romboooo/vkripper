package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.ProductRequest;
import org.example.dto.response.ProductListResponse;
import org.example.dto.response.ProductResponse;
import org.example.entity.Product;
import org.example.entity.ProductGroup;
import org.example.entity.User;
import org.example.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;
    private final UserService userService;

    @Override
    public ProductListResponse getCatalog(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> products = productRepository.findByAvailableTrue(pageable);
        return ProductListResponse.fromPage(products);
    }

    @Override
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
    public ProductListResponse getProductsByGroup(ProductGroup group, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> products = productRepository.findByProductGroupAndAvailable(group, true, pageable);
        return ProductListResponse.fromPage(products);
    }
    @Override
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

    @PreAuthorize("hasAuthority('SELLER')")
    @Override
    @Transactional
    public ProductResponse createProduct(Long userId, ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setAvailable(request.isAvailable());
        product.setProductGroup(request.getProductGroup());
        product.setSeller(userService.getUserEntityById(userId));

        Product saved = productRepository.save(product);
        return ProductResponse.fromProduct(saved);
    }

    public void validateProductCreateForProcess(String role, ProductRequest request) {
        if (!hasRole(role, "SELLER")) {
            throw new RuntimeException("Недостаточно прав для создания товара");
        }
        if (request.getPrice() == null || request.getPrice().signum() < 1) {
            throw new RuntimeException("Цена товара должна быть больше 0");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Название товара обязательно");
        }
        if (request.getProductGroup() == null) {
            throw new RuntimeException("Группа товара обязательна");
        }
    }

    @Transactional
    public ProductResponse createProductForProcess(Long userId, String role, ProductRequest request) {
        validateProductCreateForProcess(role, request);
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setAvailable(request.isAvailable());
        product.setProductGroup(request.getProductGroup());
        product.setSeller(userService.getUserEntityById(userId));

        return ProductResponse.fromProduct(productRepository.save(product));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MODERATOR') or " +
            "(hasAuthority('SELLER') and @productServiceImpl.getProductEntityById(#productId).seller.id == #userId)")
    @Transactional
    public void deleteProduct(Long userId, Long productId){
        productRepository.deleteById(productId);
    }

    public void validateProductDeleteForProcess(Long userId, String role, Long productId) {
        Product product = getProductEntityById(productId);
        boolean adminOrModerator = hasRole(role, "ADMIN") || hasRole(role, "MODERATOR");
        boolean ownerSeller = hasRole(role, "SELLER") && product.getSeller().getId() == userId;
        if (!adminOrModerator && !ownerSeller) {
            throw new RuntimeException("Недостаточно прав для удаления товара");
        }
    }

    @Transactional
    public void deleteProductForProcess(Long userId, String role, Long productId) {
        validateProductDeleteForProcess(userId, role, productId);
        productRepository.deleteById(productId);
    }

    private boolean hasRole(String actualRole, String expectedRole) {
        return expectedRole.equals(actualRole);
    }
}
