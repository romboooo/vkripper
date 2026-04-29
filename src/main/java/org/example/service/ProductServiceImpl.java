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
@Transactional
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;
    private final UserService userService;

    @Override
    @PreAuthorize("hasAuthority('BUYER') or hasAuthority('SELLER') or hasAuthority('MODERATOR') or hasAuthority('ADMIN')")
    public ProductListResponse getCatalog(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> products = productRepository.findByAvailableTrue(pageable);
        return ProductListResponse.fromPage(products);
    }

    @Override
    @PreAuthorize("hasAuthority('BUYER') or hasAuthority('SELLER') or hasAuthority('MODERATOR') or hasAuthority('ADMIN')")
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
    @PreAuthorize("hasAuthority('BUYER') or hasAuthority('SELLER') or hasAuthority('MODERATOR') or hasAuthority('ADMIN')")
    public ProductListResponse getProductsByGroup(ProductGroup group, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> products = productRepository.findByProductGroupAndAvailable(group, true, pageable);
        return ProductListResponse.fromPage(products);
    }
    @Override
    @PreAuthorize("hasAuthority('BUYER') or hasAuthority('SELLER') or hasAuthority('MODERATOR') or hasAuthority('ADMIN')")
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

    @Override
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MODERATOR') or " +
            "(hasAuthority('SELLER') and @productServiceImpl.getProductEntityById(#productId).seller.id == #userId)")
    public void deleteProduct(Long userId, Long productId){
        productRepository.deleteById(productId);
    }
}