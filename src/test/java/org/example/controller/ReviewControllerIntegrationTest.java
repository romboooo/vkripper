package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.IntegrationTestBase;
import org.example.dto.request.ReviewRequest;
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
class ReviewControllerIntegrationTest extends IntegrationTestBase {

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
        testUser.setUsername("test_user_review");
        testUser.setBalance(new BigDecimal("1000.00"));
        testUser.setFavorites(new java.util.ArrayList<>());
        testUser = userRepository.save(testUser);

        testProduct = new Product();
        testProduct.setName("Test Product for Review");
        testProduct.setPrice(new BigDecimal("50.00"));
        testProduct.setAvailable(true);
        testProduct.setProductGroup(ProductGroup.ELECTRONICS);
        testProduct.setSeller(testUser);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void shouldCreateReviewSuccessfully() throws Exception {
        ReviewRequest request = new ReviewRequest();
        request.setUserId(testUser.getId());
        request.setProductId(testProduct.getId());
        request.setText("Great product!");
        request.setRating(5);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is("Great product!")))
                .andExpect(jsonPath("$.rating", is(5)));
    }

    @Test
    void shouldReturnBadRequestForInvalidRating() throws Exception {
        ReviewRequest request = new ReviewRequest();
        request.setUserId(testUser.getId());
        request.setProductId(testProduct.getId());
        request.setText("Bad rating");
        request.setRating(10);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAllReviews() throws Exception {
        ReviewRequest request = new ReviewRequest();
        request.setUserId(testUser.getId());
        request.setProductId(testProduct.getId());
        request.setText("Test review");
        request.setRating(4);

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/reviews")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void shouldDeleteReviewSuccessfully() throws Exception {
        ReviewRequest request = new ReviewRequest();
        request.setUserId(testUser.getId());
        request.setProductId(testProduct.getId());
        request.setText("To be deleted");
        request.setRating(3);

        String response = mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long reviewId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/reviews/{id}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.containsString("deleted")));
    }
}