package org.example.service;

import org.example.IntegrationTestBase;
import org.example.dto.response.ProductListResponse;
import org.example.dto.response.ProductResponse;
import org.example.entity.Product;
import org.example.entity.ProductGroup;
import org.example.entity.User;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.example.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private User testSeller;

    @BeforeEach
    void setUp() {
        testSeller = TestDataFactory.createUser("seller_" + System.currentTimeMillis(), BigDecimal.valueOf(10000));
        testSeller = userRepository.saveAndFlush(testSeller);
    }

    @Test
    @DisplayName("Получение каталога доступных товаров")
    void shouldReturnCatalogWithAvailableProducts() {
        createProduct("Товар 1", ProductGroup.ELECTRONICS, true);
        createProduct("Товар 2", ProductGroup.HOME_AND_DACHA, true);
        createProduct("Недоступный", ProductGroup.ELECTRONICS, false);

        ProductListResponse response = productService.getCatalog(0, 10);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent()).allMatch(ProductResponse::isAvailable);
        assertThat(response.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Поиск товаров по названию")
    void shouldSearchProductsByName() {
        createProduct("Смартфон Samsung", ProductGroup.ELECTRONICS, true);
        createProduct("Ноутбук Samsung", ProductGroup.ELECTRONICS, true);
        createProduct("Кроссовки", ProductGroup.SPORT_AND_LEISURE, true);

        List<ProductResponse> results = productService.searchProducts("samsung");

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(p -> p.getName().toLowerCase().contains("samsung"));
    }

    @Test
    @DisplayName("Фильтрация по группе товаров")
    void shouldReturnProductsByGroup() {
        createProduct("Телефон", ProductGroup.ELECTRONICS, true);
        createProduct("Футболка", ProductGroup.WARDROBE, true);
        createProduct("Наушники", ProductGroup.ELECTRONICS, true);

        ProductListResponse response = productService.getProductsByGroup(ProductGroup.ELECTRONICS, 0, 10);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent()).allMatch(p -> p.getProductGroup() == ProductGroup.ELECTRONICS);
    }

    @Test
    @DisplayName("Получение товара по ID")
    void shouldReturnProductById() {
        Product product = createProduct("Уникальный товар", ProductGroup.BEAUTY, true);

        ProductResponse result = productService.getProductById(product.getId());

        assertThat(result.getId()).isEqualTo(product.getId());
        assertThat(result.getName()).isEqualTo("Уникальный товар");
    }

    @Test
    @DisplayName("Исключение при получении несуществующего товара")
    void shouldThrowExceptionWhenProductNotFound() {
        assertThatThrownBy(() -> productService.getProductById(999999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("Пагинация каталога")
    void shouldApplyPagination() {
        for (int i = 0; i < 25; i++) {
            createProduct("Товар " + i, ProductGroup.ELECTRONICS, true);
        }

        ProductListResponse page1 = productService.getCatalog(0, 10);
        ProductListResponse page2 = productService.getCatalog(1, 10);

        assertThat(page1.getContent()).hasSize(10);
        assertThat(page2.getContent()).hasSize(10);
        assertThat(page1.getTotalPages()).isEqualTo(3);
        assertThat(page1.isLast()).isFalse();
        assertThat(page2.isLast()).isFalse();
    }

    private Product createProduct(String name, ProductGroup group, boolean available) {
        Product product = TestDataFactory.createProduct(name, BigDecimal.valueOf(100), group, testSeller);
        product.setAvailable(available);
        return productRepository.saveAndFlush(product);
    }
}