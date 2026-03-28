package org.example.controller;

import org.example.IntegrationTestBase;
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
        // Создаём продавца с уникальным именем
        User seller = new User();
        seller.setUsername("seller_" + System.currentTimeMillis());
        seller.setBalance(BigDecimal.TEN);
        seller = userRepository.saveAndFlush(seller);

        // Создаём товар
        Product product = new Product();
        product.setName("Тестовый товар");
        product.setPrice(new BigDecimal("999.99"));
        product.setAvailable(true);
        product.setProductGroup(ProductGroup.ELECTRONICS);
        product.setSeller(seller);
        product = productRepository.saveAndFlush(product);

        // Запрос с ответом в String, чтобы избежать JsonParseException
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/products/" + product.getId(),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull().isNotEmpty();
        assertThat(body).contains(String.valueOf(product.getId()));
        assertThat(body).contains("Тестовый товар");
        assertThat(body).contains("999.99");
        assertThat(body).contains("ELECTRONICS");
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
}