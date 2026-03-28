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
        User seller = new User();
        seller.setUsername("test_seller");
        seller.setBalance(BigDecimal.TEN);
        seller = userRepository.save(seller);

        Product product = new Product();
        product.setName("Тестовый товар");
        product.setPrice(new BigDecimal("999.99"));
        product.setAvailable(true);
        product.setProductGroup(ProductGroup.ELECTRONICS);
        product.setSeller(seller);
        product = productRepository.save(product);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/products/" + product.getId(),
                String.class
        );

        String body = response.getBody();
        assertThat(body).isNotNull().isNotEmpty();
        assertThat(body).contains("id");
        assertThat(body).contains("name");
    }

}