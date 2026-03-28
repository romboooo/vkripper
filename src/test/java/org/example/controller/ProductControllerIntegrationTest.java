package org.example.controller;

import org.example.IntegrationTestBase;
import org.example.dto.response.ProductResponse;
import org.example.entity.Product;
import org.example.entity.ProductGroup;
import org.example.entity.User;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnProductById() {
        User seller = createUser();
        Product product = createProduct(seller);

        ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
                "/api/products/" + product.getId(),
                ProductResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(product.getId());
        assertThat(response.getBody().getName()).isEqualTo("Тестовый товар");
        assertThat(response.getBody().getPrice()).isEqualTo(new BigDecimal("999.99"));
        assertThat(response.getBody().isAvailable()).isTrue();
    }

    @Test
    void shouldReturnNotFoundForNonExistentProduct() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/products/999999",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("не найден");
    }

    private User createUser() {
        User user = new User();
        user.setUsername("seller_" + System.currentTimeMillis());
        user.setBalance(BigDecimal.valueOf(1000));
        return userRepository.saveAndFlush(user);
    }

    private Product createProduct(User seller) {
        Product product = new Product();
        product.setName("Тестовый товар");
        product.setPrice(new BigDecimal("999.99"));
        product.setAvailable(true);
        product.setProductGroup(ProductGroup.ELECTRONICS);
        product.setSeller(seller);
        return productRepository.saveAndFlush(product);
    }
}