package org.example.service;

import org.example.IntegrationTestBase;
import org.example.dto.response.ProductResponse;
import org.example.entity.Product;
import org.example.entity.ProductGroup;
import org.example.entity.User;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.example.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    private User testSeller;

    @BeforeEach
    void setUp() {
        testSeller = TestDataFactory.createUser("test_seller", BigDecimal.valueOf(10000));
        testSeller = userRepository.save(testSeller);
    }

    @Test
    void shouldReturnProductById() {
        // Arrange
        Product product = TestDataFactory.createProduct(
                "Тестовый товар",
                BigDecimal.valueOf(999.99),
                ProductGroup.ELECTRONICS,
                testSeller
        );
        product = productRepository.save(product);

        // Act
        ProductResponse result = productService.getProductById(product.getId());

        // Assert
        assertThat(result.getName()).isEqualTo("Тестовый товар");
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(999.99));
        assertThat(result.isAvailable()).isTrue();
    }
}