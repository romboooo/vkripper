package org.example.controller;

import org.example.IntegrationTestBase;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnProductById() throws Exception {
        // Создаём продавца
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

        // Проверка через jsonPath (надежнее десериализации)
        mockMvc.perform(get("/api/products/" + product.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name").value("Тестовый товар"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.productGroup").value("ELECTRONICS"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/products/999999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error", org.hamcrest.Matchers.containsString("не найден")));
    }
}