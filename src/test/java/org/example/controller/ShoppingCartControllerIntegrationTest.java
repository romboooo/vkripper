package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.IntegrationTestBase;
import org.example.dto.request.ShoppingCartRequest;
import org.example.entity.Product;
import org.example.entity.ProductGroup;
import org.example.entity.User;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ShoppingCartControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        productRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("test_user_cart");
        testUser.setBalance(new BigDecimal("1000.00"));
        testUser.setFavorites(new java.util.ArrayList<>());
        testUser = userRepository.save(testUser);

        testProduct = new Product();
        testProduct.setName("Test Product for Cart");
        testProduct.setPrice(new BigDecimal("50.00"));
        testProduct.setAvailable(true);
        testProduct.setProductGroup(ProductGroup.ELECTRONICS);
        testProduct.setSeller(testUser);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void shouldAddItemToCartSuccessfully() throws Exception {
        ShoppingCartRequest request = new ShoppingCartRequest();
        request.setUserId(testUser.getId());
        request.setProductId(testProduct.getId());

        mockMvc.perform(post("/api/shoppingCart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(1)))
                .andExpect(jsonPath("$.product.id", is(testProduct.getId().intValue())))
                .andExpect(jsonPath("$.user.id", is((int) testUser.getId())));
    }

    @Test
    void shouldReturnBadRequestWhenAddingDuplicateItem() throws Exception {
        ShoppingCartRequest request = new ShoppingCartRequest();
        request.setUserId(testUser.getId());
        request.setProductId(testProduct.getId());

        mockMvc.perform(post("/api/shoppingCart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/api/shoppingCart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // Ожидаем 400 от GlobalExceptionHandler
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldGetShoppingCartItems() throws Exception {
        ShoppingCartRequest request = new ShoppingCartRequest();
        request.setUserId(testUser.getId());
        request.setProductId(testProduct.getId());

        mockMvc.perform(post("/api/shoppingCart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/shoppingCart")
                        .param("userId", String.valueOf(testUser.getId()))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].product.id", is(testProduct.getId().intValue())))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void shouldDeleteItemFromCart() throws Exception {
        ShoppingCartRequest request = new ShoppingCartRequest();
        request.setUserId(testUser.getId());
        request.setProductId(testProduct.getId());

        mockMvc.perform(post("/api/shoppingCart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(delete("/api/shoppingCart/{productId}", testProduct.getId())
                        .param("userId", String.valueOf(testUser.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.containsString("Удалили")));
    }
}