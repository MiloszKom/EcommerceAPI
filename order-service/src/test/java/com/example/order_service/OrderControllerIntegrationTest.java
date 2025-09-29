package com.example.order_service;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.order_service.client.CartClient;
import com.example.order_service.client.ProductClient;
import com.example.order_service.dto.CartDetailsDTO;
import com.example.order_service.dto.CartItemDTO;
import com.example.order_service.dto.ProductDTO;
import com.example.order_service.exception.types.RemoteServiceException;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderItem;
import com.example.order_service.model.OrderStatus;
import com.example.order_service.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class OrderControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private CartClient cartClient;

    @MockBean
    private ProductClient productClient;

    @Value("${app.gateway.secret}")
    private String gatewaySecret;

    private long order1Id;
    private long order2Id;
    private long order3Id;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        Order order1 = new Order(
                1L,
                new BigDecimal("100.00"),
                OrderStatus.PENDING,
                List.of(new OrderItem(101L, "Example Product", 2, new BigDecimal("50.00")))
        );
        order1Id = orderRepository.save(order1).getId();

        Order order2 = new Order(
                2L,
                new BigDecimal("400.00"),
                OrderStatus.PAID,
                List.of(new OrderItem(202L, "Example Product 2", 4, new BigDecimal("100.00")))
        );
        order2Id = orderRepository.save(order2).getId();

        Order order3 = new Order(
                3L,
                new BigDecimal("800.00"),
                OrderStatus.COMPLETED,
                List.of(new OrderItem(303L, "Example Product 3", 8, new BigDecimal("100.00")))
        );
        order2Id = orderRepository.save(order2).getId();
    }

    @Test
    void callingEndpointWithoutInternalSecret_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createOrder_shouldReturn400_whenCartIsEmpty () throws Exception {
        Mockito.when(cartClient.getUserCart())
                .thenReturn(new CartDetailsDTO(1L, new ArrayList<CartItemDTO>(), 0));

        mockMvc.perform(post("/api/orders")
                .header("X-Internal-Secret", gatewaySecret)
                .header("userId", 1)
                .header("role", "ROLE_USER"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_shouldReturn201_whenCartHasItems() throws Exception {
        CartItemDTO cartItem = new CartItemDTO(1L, 2);
        Mockito.when(cartClient.getUserCart())
                .thenReturn(new CartDetailsDTO(1L, List.of(cartItem), 2));

        Mockito.when(productClient.getProductById(1L))
                .thenReturn(new ProductDTO(1L, "Test Product", "Test Description", "test-category", new BigDecimal("100.00"), 2));

        Mockito.doNothing().when(productClient).reduceProductStock(Mockito.eq(1L), Mockito.any());

        mockMvc.perform(post("/api/orders")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1)
                        .header("role", "ROLE_USER"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value("200.0"))
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].productName").value("Test Product"));
    }

    @Test
    void createOrder_shouldReturn503_whenCartServiceUnavailable() throws Exception {
        Mockito.when(cartClient.getUserCart())
                .thenThrow(new RemoteServiceException("product-service",
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Product service is unavailable"));;

        mockMvc.perform(post("/api/orders")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1)
                        .header("role", "ROLE_USER"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void payOrder_returns200_whenSuccessful() throws Exception {
        mockMvc.perform(post("/api/orders/" + order1Id + "/pay")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1)
                        .header("role", "ROLE_USER"))
                .andExpect(status().isOk());
    }

    @Test
    void payOrder_throwsNotFound_whenOrderDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/orders/" + 999 + "/pay")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1)
                        .header("role", "ROLE_USER"))
                .andExpect(status().isNotFound());
    }

    @Test
    void payOrder_throwsUnauthorized_whenUserDoesNotOwnOrder() throws Exception {
        mockMvc.perform(post("/api/orders/" + order1Id + "/pay")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 999)
                        .header("role", "ROLE_USER"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void payOrder_throwsConflict_whenOrderIsNotPending() throws Exception {
        mockMvc.perform(post("/api/orders/" + order2Id + "/pay")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 2)
                        .header("role", "ROLE_USER"))
                .andExpect(status().isConflict());
    }

    @Test
    void getUserOrders_returns200() throws Exception {
        mockMvc.perform(get("/api/orders/me")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1)
                        .header("role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getOrderDetails_returns200_whenUserOwnsOrder() throws Exception {
        mockMvc.perform(get("/api/orders/" + order1Id)
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1)
                        .header("role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order1Id))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(101));
    }

    @Test
    void getOrderDetails_returns200_whenAdminAccessesAnotherUsersOrder() throws Exception {
        mockMvc.perform(get("/api/orders/" + order1Id)
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 999)
                        .header("role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order1Id));
    }

    @Test
    void getOrderDetails_throwsUnauthorized_whenUserDoesNotOwnOrder() throws Exception {
        mockMvc.perform(get("/api/orders/" + order1Id)
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 999)
                        .header("role", "ROLE_USER"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cancelOrder_returns200_whenUserCancelsOwnPendingOrder() throws Exception {
        mockMvc.perform(post("/api/orders/" + order1Id + "/cancel")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1)
                        .header("role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order1Id))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelOrder_returns200_whenAdminCancelsAnotherUsersOrder() throws Exception {
        mockMvc.perform(post("/api/orders/" + order1Id + "/cancel")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 999)
                        .header("role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order1Id))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelOrder_throwsAccessDenied_whenUserDoesNotOwnOrder() throws Exception {
        mockMvc.perform(post("/api/orders/" + order2Id + "/cancel")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 999)
                        .header("role", "ROLE_USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelOrder_throwsBadRequest_whenOrderIsNotPending() throws Exception {
        mockMvc.perform(post("/api/orders/" + order2Id + "/cancel")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 2)
                        .header("role", "ROLE_USER"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeOrderAsAdmin_returns200_whenOrderIsPaid() throws Exception {
        mockMvc.perform(post("/api/orders/" + order2Id + "/complete")
                        .header("X-Internal-Secret", gatewaySecret))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order2Id))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void completeOrderAsAdmin_throwsBadRequest_whenOrderIsNotPaid() throws Exception {
        mockMvc.perform(post("/api/orders/" + order1Id + "/complete")
                        .header("X-Internal-Secret", gatewaySecret))
                .andExpect(status().isBadRequest());
    }
}
