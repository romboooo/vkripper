package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.IntegrationTestBase;
import org.example.dto.request.PurchaseRequest;
import org.example.entity.*;
import org.example.repository.ProductRepository;
import org.example.repository.ShoppingCartRepository;
import org.example.repository.UserRepository;
import org.example.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class PurchaseControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    private Product testProduct;
    private ShoppingCart testCartItem;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        productRepository.deleteAll();
        shoppingCartRepository.deleteAll();
        SecurityContextHolder.clearContext();

        User testUser = new User();
        testUser.setUsername("test_user_purchase");
        testUser.setPassword("encoded");
        testUser.setBalance(new BigDecimal("1000.00"));
        testUser.setRole(Role.BUYER);
        testUser = userRepository.save(testUser);

        User seller = new User();
        seller.setUsername("test_seller");
        seller.setPassword("encoded");
        seller.setBalance(BigDecimal.ZERO);
        seller.setRole(Role.SELLER);
        seller = userRepository.save(seller);

        testProduct = new Product();
        testProduct.setName("Test Product for Purchase");
        testProduct.setPrice(new BigDecimal("50.00"));
        testProduct.setAvailable(true);
        testProduct.setProductGroup(ProductGroup.ELECTRONICS);
        testProduct.setSeller(seller);
        testProduct = productRepository.save(testProduct);

        testCartItem = new ShoppingCart();
        testCartItem.setUser(testUser);
        testCartItem.setProduct(testProduct);
        testCartItem.setAmountInCart(2);
        testCartItem = shoppingCartRepository.save(testCartItem);

        CustomUserDetails userDetails = new CustomUserDetails(
                testUser.getId(),
                testUser.getUsername(),
                testUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(testUser.getRole().name()))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreatePurchaseWithBalanceSuccessfully() throws Exception {
        PurchaseRequest request = new PurchaseRequest();
        request.setCartItemId(testCartItem.getId());
        request.setPurchaseType(PurchaseType.BALANCE);
        request.setAmountInPurchase(1);

        MvcResult result = mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        System.out.println("STATUS: " + result.getResponse().getStatus());
        System.out.println("BODY: " + result.getResponse().getContentAsString());

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())   // <-- добавить
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.message", is("Покупка успешно оформлена")));
    }

    @Test
    void shouldCreatePurchaseWithSellerSuccessfully() throws Exception {
        PurchaseRequest request = new PurchaseRequest();
        request.setCartItemId(testCartItem.getId());
        request.setPurchaseType(PurchaseType.SELLER);
        request.setAmountInPurchase(1);
        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void shouldCreatePurchaseWithOzonSuccessfully() throws Exception {
        PurchaseRequest request = new PurchaseRequest();
        request.setCartItemId(testCartItem.getId());
        request.setPurchaseType(PurchaseType.OZON);
        request.setAmountInPurchase(1);
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