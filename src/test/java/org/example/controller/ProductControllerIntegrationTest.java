package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.response.ProductResponse;
import org.example.entity.Product;
import org.example.entity.ProductGroup;
import org.example.entity.User;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void shouldReturnProductById() throws Exception {
        // Arrange
        User seller = new User();
        seller.setUsername("seller_" + System.currentTimeMillis());
        seller.setBalance(BigDecimal.TEN);
        seller = userRepository.saveAndFlush(seller);

        Product product = new Product();
        product.setName("Тестовый товар");
        product.setPrice(new BigDecimal("999.99"));
        product.setAvailable(true);
        product.setProductGroup(ProductGroup.ELECTRONICS);
        product.setSeller(seller);
        product = productRepository.saveAndFlush(product);

        // Act & Assert
        String responseJson = mockMvc.perform(get("/api/products/" + product.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProductResponse body = objectMapper.readValue(responseJson, ProductResponse.class);

        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo(product.getId());
        assertThat(body.getName()).isEqualTo("Тестовый товар");
        assertThat(body.getPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(body.getProductGroup()).isEqualTo(ProductGroup.ELECTRONICS);
    }

    @Test
    void shouldReturnNotFoundForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/products/999999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()) // или isNotFound, зависит от GlobalExceptionHandler
                .andExpect(result ->
                        assertThat(result.getResponse().getContentAsString())
                                .contains("не найден"));
    }
}