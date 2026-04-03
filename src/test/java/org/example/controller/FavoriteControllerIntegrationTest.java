package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.IntegrationTestBase;
import org.example.dto.request.FavoriteRequest;
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
class FavoriteControllerIntegrationTest extends IntegrationTestBase {

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
        testUser.setUsername("test_user_favorite");
        testUser.setBalance(new BigDecimal("1000.00"));
        testUser.setFavorites(new java.util.ArrayList<>());
        testUser = userRepository.save(testUser);

        testProduct = new Product();
        testProduct.setName("Test Product for Favorite");
        testProduct.setPrice(new BigDecimal("50.00"));
        testProduct.setAvailable(true);
        testProduct.setProductGroup(ProductGroup.ELECTRONICS);
        testProduct.setSeller(testUser);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void shouldAddToFavoriteSuccessfully() throws Exception {
        FavoriteRequest request = new FavoriteRequest();
        request.setUserId(testUser.getId());
        request.setProductId(testProduct.getId());

        mockMvc.perform(post("/api/favorite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testProduct.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Test Product for Favorite")));
    }

    @Test
    void shouldReturnBadRequestForDuplicateFavorite() throws Exception {
        FavoriteRequest request = new FavoriteRequest();
        request.setUserId(testUser.getId());
        request.setProductId(testProduct.getId());

        mockMvc.perform(post("/api/favorite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/api/favorite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldGetFavoritesSuccessfully() throws Exception {
        FavoriteRequest request = new FavoriteRequest();
        request.setUserId(testUser.getId());
        request.setProductId(testProduct.getId());

        mockMvc.perform(post("/api/favorite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/favorite")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void shouldDeleteFromFavoriteSuccessfully() throws Exception {
        FavoriteRequest request = new FavoriteRequest();
        request.setUserId(testUser.getId());
        request.setProductId(testProduct.getId());

        mockMvc.perform(post("/api/favorite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(delete("/api/favorite/{productId}", testProduct.getId())
                        .param("userId", String.valueOf(testUser.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.containsString("Удалили")));
    }
}