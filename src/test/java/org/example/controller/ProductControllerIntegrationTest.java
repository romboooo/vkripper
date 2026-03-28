package org.example.controller;

import org.example.IntegrationTestBase;
import org.example.dto.response.ProductResponse;
import org.example.entity.Product;
import org.example.entity.ProductGroup;
import org.example.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Sql(scripts = "classpath:/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Test
    void shouldReturnProductById() {
        Product product = new Product();
        product.setName("Тестовый товар");
        product.setPrice(new BigDecimal("999.99"));
        product.setAvailable(true);
        product.setProductGroup(ProductGroup.ELECTRONICS);
        product = productRepository.save(product);

        ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
                "/api/products/" + product.getId(),
                ProductResponse.class
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getName()).isEqualTo("Тестовый товар");
    }
}