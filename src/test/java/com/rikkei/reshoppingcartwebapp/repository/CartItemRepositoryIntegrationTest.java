package com.rikkei.reshoppingcartwebapp.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import com.rikkei.reshoppingcartwebapp.model.Cart;
import com.rikkei.reshoppingcartwebapp.model.CartItem;
import com.rikkei.reshoppingcartwebapp.model.Product;
import com.rikkei.reshoppingcartwebapp.model.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class CartItemRepositoryIntegrationTest {

    @Container
    static final MySQLContainer mysql = new MySQLContainer("mysql:8.4")
            .withDatabaseName("defaultdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("db/schema-test.sql");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", mysql::getJdbcUrl);
        registry.add("DB_USERNAME", mysql::getUsername);
        registry.add("DB_PASSWORD", mysql::getPassword);
    }

    @Autowired UserRepository userRepository;
    @Autowired CartRepository cartRepository;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired ProductRepository productRepository;
    @LocalServerPort int port;

    private Cart firstCart;
    private Cart secondCart;
    private Product product;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        User firstUser = userRepository.save(new User(null, "First user", null));
        User secondUser = userRepository.save(new User(null, "Second user", null));
        firstCart = cartRepository.save(new Cart(firstUser, null));
        secondCart = cartRepository.save(new Cart(secondUser, null));
        product = productRepository.save(new Product("Phone case", "case.jpg", new BigDecimal("155000.00")));
    }

    @Test
    void findByCartId_returnsOnlyThatCartsItems() {
        cartItemRepository.save(new CartItem(firstCart, product, 2));
        cartItemRepository.save(new CartItem(secondCart, product, 1));

        assertThat(cartItemRepository.findByCartId(firstCart.getId()))
                .singleElement()
                .satisfies(item -> assertThat(item.getQuantity()).isEqualTo(2));
    }

    @Test
    void findByCartIdAndProductId_returnsMatchOrEmpty() {
        cartItemRepository.save(new CartItem(firstCart, product, 1));

        assertThat(cartItemRepository.findByCartIdAndProductId(firstCart.getId(), product.getId())).isPresent();
        assertThat(cartItemRepository.findByCartIdAndProductId(secondCart.getId(), product.getId())).isEmpty();
    }

    @Test
    void deleteByCartId_deletesOnlyMatchingCartItems() {
        cartItemRepository.save(new CartItem(firstCart, product, 2));
        cartItemRepository.save(new CartItem(secondCart, product, 1));

        cartItemRepository.deleteByCartId(firstCart.getId());

        assertThat(cartItemRepository.findByCartId(firstCart.getId())).isEmpty();
        assertThat(cartItemRepository.findByCartId(secondCart.getId())).hasSize(1);
    }

    @Test
    void uniqueConstraint_rejectsDuplicateCartProductPair() {
        cartItemRepository.saveAndFlush(new CartItem(firstCart, product, 1));

        assertThatThrownBy(() -> cartItemRepository.saveAndFlush(new CartItem(firstCart, product, 2)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void decimalPrice_roundTripsWithoutPrecisionLoss() {
        Product reloaded = productRepository.findById(product.getId()).orElseThrow();

        assertThat(reloaded.getPrice()).isEqualByComparingTo("155000.00");
        assertThat(reloaded.getName()).isEqualTo("Phone case");
    }

    @Test
    void healthProbes_areExposedButSensitiveActuatorEndpointsAreNot() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        assertThat(get(client, "/actuator/health/liveness").statusCode()).isEqualTo(200);
        assertThat(get(client, "/actuator/health/readiness").statusCode()).isEqualTo(200);
        assertThat(get(client, "/actuator/health/readiness").body()).contains("\"status\":\"UP\"");
        assertThat(get(client, "/actuator/env").statusCode()).isEqualTo(404);
    }

    private HttpResponse<String> get(HttpClient client, String path) throws Exception {
        return client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
