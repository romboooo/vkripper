package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.IntegrationTestBase;
import org.example.dto.request.PurchaseRequest;
import org.example.entity.*;
import org.example.repository.ProductRepository;
import org.example.repository.ShoppingCartRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class PurchaseControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    private User testUser;
    private Product testProduct;
    private ShoppingCart testCartItem;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        productRepository.deleteAll();
        shoppingCartRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("test_user_purchase");
        testUser.setBalance(new BigDecimal("1000.00"));
        testUser.setFavorites(new java.util.ArrayList<>());
        testUser = userRepository.save(testUser);

        testProduct = new Product();
        testProduct.setName("Test Product for Purchase");
        testProduct.setPrice(new BigDecimal("50.00"));
        testProduct.setAvailable(true);
        testProduct.setProductGroup(ProductGroup.ELECTRONICS);
        testProduct.setSeller(testUser);
        testProduct = productRepository.save(testProduct);

        testCartItem = new ShoppingCart();
        testCartItem.setUser(testUser);
        testCartItem.setProduct(testProduct);
        testCartItem.setAmount(2);
        testCartItem = shoppingCartRepository.save(testCartItem);
    }

    @Test
    void shouldCreatePurchaseWithBalanceSuccessfully() throws Exception {
        PurchaseRequest request = new PurchaseRequest();
        request.setUserId(testUser.getId());
        request.setCartItemId(testCartItem.getId());
        request.setPurchaseType(PurchaseType.BALANCE);

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.message", is("Покупка успешно оформлена")));
    }

    @Test
    void shouldCreatePurchaseWithSellerSuccessfully() throws Exception {
        PurchaseRequest request = new PurchaseRequest();
        request.setUserId(testUser.getId());
        request.setCartItemId(testCartItem.getId());
        request.setPurchaseType(PurchaseType.SELLER);

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void shouldCreatePurchaseWithOzonSuccessfully() throws Exception {
        PurchaseRequest request = new PurchaseRequest();
        request.setUserId(testUser.getId());
        request.setCartItemId(testCartItem.getId());
        request.setPurchaseType(PurchaseType.OZON);

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REDIRECT")))
                .andExpect(jsonPath("$.redirectUrl", is("https://ozon.ru/t/yCwkBpx")));
    }

    @Test
    void shouldReturnBadRequestForInvalidPurchaseRequest() throws Exception {
        PurchaseRequest request = new PurchaseRequest();

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}