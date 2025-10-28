package com.example.order_service.controller;

import com.example.order_service.dto.ErrorResponseDto;
import com.example.order_service.dto.OrderDetailsDto;
import com.example.order_service.dto.OrderSummaryDto;
import com.example.order_service.exception.GlobalExceptionHandler;
import com.example.order_service.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Validated
@ApiResponses({
        @ApiResponse(
                responseCode = "401",
                description = "Authentication is required to access this resource.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponseDto.class),
                        examples = @ExampleObject(
                                name = "Unauthorized Response",
                                value = """
                {
                  "statusCode": 401,
                  "message": "Authentication is required to access this resource.",
                  "timestamp": "2025-10-27T15:10:00",
                  "path": "/api/orders"
                }
                """
                        )
                )
        )
})
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new order",
            description = "Creates a new order for the current user based on their cart.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Order successfully created",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDetailsDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                       "id": 4,
                                       "userId": "345c8ef4-e13f-4848-b770-9a5267965d62",
                                       "items": [
                                         {
                                           "id": 4,
                                           "productId": 9,
                                           "productName": "productName",
                                           "quantity": 1,
                                           "priceAtPurchase": 1499.99
                                         }
                                       ],
                                       "status": "PENDING",
                                       "totalPrice": 1499.99,
                                       "createdAt": "2025-10-27T23:05:13.350162",
                                       "updatedAt": "2025-10-27T23:05:13.350162"
                                     }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                         "statusCode": 400,
                                         "message": "Cannot create order: cart is empty.",
                                         "timestamp": "2025-10-27T23:09:03.2989752",
                                         "path": "/api/orders"
                                    }
                                    """)
                            )
                    ),
            }
    )
    public ResponseEntity<OrderDetailsDto> createOrder(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("POST /api/orders called by userId={}", userId);

        OrderDetailsDto newOrder = orderService.createOrder(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
    }

    @PostMapping("/{orderId}/pay")
    @Operation(
            summary = "Pay for an order",
            description = "Processes payment for a specific order by the current user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order successfully paid",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDetailsDto.class),
                                    examples = @ExampleObject(value = """
                                {
                                    "id": 3,
                                    "userId": "345c8ef4-e13f-4848-b770-9a5267965d62",
                                    "items": [
                                      {
                                        "id": 3,
                                        "productId": 9,
                                        "productName": "productName",
                                        "quantity": 2,
                                        "priceAtPurchase": 1499.99
                                      }
                                    ],
                                    "status": "PAID",
                                    "totalPrice": 2999.98,
                                    "createdAt": "2025-10-27T00:30:06.570163",
                                    "updatedAt": "2025-10-27T23:11:13.3113032"
                                  }
                                """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access Denied",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                      "statusCode": 403,
                                      "message": "You do not have permission to pay for this order.",
                                      "timestamp": "2025-10-27T23:14:18.2860874",
                                      "path": "/api/orders/1/pay"
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Order Not Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                       "statusCode": 404,
                                       "message": "Cannot find order with id: 999",
                                       "timestamp": "2025-10-27T23:12:52.2912816",
                                       "path": "/api/orders/999/pay"
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict - Order Already Paid",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                       "statusCode": 409,
                                       "message": "Order cannot be paid. Current status: PAID",
                                       "timestamp": "2025-10-27T23:14:56.3735863",
                                       "path": "/api/orders/3/pay"
                                     }
                                    """)
                            )
                    ),
            }

    )
    public ResponseEntity<OrderDetailsDto> payOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("orderId") Long orderId
    ) {
        String userId = jwt.getSubject();
        log.info("POST /api/orders/{}/pay called by userId={}", orderId, userId);

        OrderDetailsDto order = orderService.payOrder(userId, orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user's orders",
            description = "Fetches a list of all orders placed by the current user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User Orders retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDetailsDto.class),
                                    examples = @ExampleObject(value = """
                                    [
                                        {
                                          "id": 3,
                                          "status": "PAID",
                                          "totalPrice": 2999.98,
                                          "numberOfItems": 2,
                                          "createdAt": "2025-10-27T00:30:06.570163"
                                        },
                                        {
                                          "id": 4,
                                          "status": "PAID",
                                          "totalPrice": 1499.99,
                                          "numberOfItems": 1,
                                          "createdAt": "2025-10-27T23:05:13.350162"
                                        }
                                      ]
                                    """)
                            )
                    )}
    )
    public ResponseEntity<List<OrderSummaryDto>> getCurrentUserOrders(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("GET /api/orders/me called by userId={}", userId);

        List<OrderSummaryDto> orders = orderService.getCurrentUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    @Operation(
            summary = "Get order details",
            description = "Fetches details of a specific order. Admins can access any order.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDetailsDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                          "id": 3,
                                          "userId": "345c8ef4-e13f-4848-b770-9a5267965d62",
                                          "items": [
                                            {
                                              "id": 3,
                                              "productId": 9,
                                              "productName": "productName",
                                              "quantity": 2,
                                              "priceAtPurchase": 1499.99
                                            }
                                          ],
                                          "status": "PAID",
                                          "totalPrice": 2999.98,
                                          "createdAt": "2025-10-27T00:30:06.570163",
                                          "updatedAt": "2025-10-27T23:11:13.311303"
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access Denied",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                      "statusCode": 403,
                                      "message": "You cannot access this order.",
                                      "timestamp": "2025-10-27T23:14:18.2860874",
                                      "path": "/api/orders/1"
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Order Not Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                       "statusCode": 404,
                                       "message": "Cannot find order with id: 999",
                                       "timestamp": "2025-10-27T23:19:42.0778332",
                                       "path": "/api/orders/999"
                                     }
                                    """)
                            )
                    )}
    )
    public ResponseEntity<OrderDetailsDto> getOrderDetails(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        String userId = jwt.getSubject();
        boolean isAdmin = isAdmin(authentication);
        log.info("GET /api/orders/{} called by userId={}, isAdmin={}", orderId, userId, isAdmin);

        OrderDetailsDto order = orderService.getOrderDetails(userId, isAdmin, orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(
            summary = "Cancel an order",
            description = "Cancels a specific order. Users can cancel their orders; admins can cancel any order.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order cancelled successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDetailsDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                          "id": 3,
                                          "userId": "345c8ef4-e13f-4848-b770-9a5267965d62",
                                          "items": [
                                            {
                                              "id": 3,
                                              "productId": 9,
                                              "productName": "productName",
                                              "quantity": 2,
                                              "priceAtPurchase": 1499.99
                                            }
                                          ],
                                          "status": "CANCELLED",
                                          "totalPrice": 2999.98,
                                          "createdAt": "2025-10-27T00:30:06.570163",
                                          "updatedAt": "2025-10-27T23:11:13.311303"
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access Denied",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                      "statusCode": 403,
                                      "message": "You cannot access this order.",
                                      "timestamp": "2025-10-27T23:14:18.2860874",
                                      "path": "/api/orders/1"
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict - Order Already Paid",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                       "statusCode": 409,
                                       "message": "Order cannot be cancelled. Current status: PAID",
                                       "timestamp": "2025-10-27T23:14:56.3735863",
                                       "path": "/api/orders/3/cancel"
                                     }
                                    """)
                            )
                    )
            }
    )
    public ResponseEntity<OrderDetailsDto> cancelOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        String userId = jwt.getSubject();
        boolean isAdmin = isAdmin(authentication);
        log.info("POST /api/orders/{}/cancel called by userId={}, isAdmin={}", orderId, userId, isAdmin);

        OrderDetailsDto order = orderService.cancelOrder(userId, isAdmin, orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @Operation(
            summary = "Get all orders (Admin only)",
            description = "Fetches all orders in the system. Only accessible to admins.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Orders retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDetailsDto.class),
                                    examples = @ExampleObject(value = """
                                    [
                                       {
                                         "id": 1,
                                         "status": "COMPLETED",
                                         "totalPrice": 14999.9,
                                         "numberOfItems": 10,
                                         "createdAt": "2025-10-17T00:54:00.114948"
                                       },
                                       {
                                         "id": 2,
                                         "status": "PENDING",
                                         "totalPrice": 14999.9,
                                         "numberOfItems": 10,
                                         "createdAt": "2025-10-17T01:08:41.437651"
                                       },
                                       {
                                         "id": 3,
                                         "status": "PAID",
                                         "totalPrice": 2999.98,
                                         "numberOfItems": 2,
                                         "createdAt": "2025-10-27T00:30:06.570163"
                                       },
                                      ]
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access Denied",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                      "statusCode": 403,
                                      "message": "Access denied: You do not have the required privileges to access this resource",
                                      "timestamp": "2025-10-27T23:14:18.2860874",
                                      "path": "/api/orders"
                                    }
                                    """)
                            )
                    )
            }
    )
    public ResponseEntity<List<OrderSummaryDto>> getAllOrders(@AuthenticationPrincipal Jwt jwt) {
        log.info("GET /api/orders called to fetch all orders (admin only)");

        List<OrderSummaryDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/complete")
    @Operation(
            summary = "Complete an order (Admin only)",
            description = "Marks a specific order as completed. Admin-only operation.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order completed successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDetailsDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                       "id": 4,
                                       "userId": "345c8ef4-e13f-4848-b770-9a5267965d62",
                                       "items": [
                                         {
                                           "id": 4,
                                           "productId": 9,
                                           "productName": "productName",
                                           "quantity": 1,
                                           "priceAtPurchase": 1499.99
                                         }
                                       ],
                                       "status": "COMPLETED",
                                       "totalPrice": 1499.99,
                                       "createdAt": "2025-10-27T23:05:13.350162",
                                       "updatedAt": "2025-10-27T23:32:37.2004929"
                                     }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access Denied",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                      "statusCode": 403,
                                      "message": "Access denied: You do not have the required privileges to access this resource",
                                      "timestamp": "2025-10-27T23:14:18.2860874",
                                      "path": "/api/orders"
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Order Not Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                       "statusCode": 404,
                                       "message": "Cannot find order with id: 999",
                                       "timestamp": "2025-10-27T23:12:52.2912816",
                                       "path": "/api/orders/999/complete"
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict - Order Already Paid",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "statusCode": 409,
                                        "message": "Only paid orders can be marked as completed. Current status: PENDING",
                                        "timestamp": "2025-10-27T23:30:56.2258842",
                                        "path": "/api/orders/2/complete"
                                    }
                                    """)
                            )
                    )
            }
    )
    public ResponseEntity<OrderDetailsDto> completeOrderAsAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId
    ) {
        log.info("POST /api/orders/{}/complete called by admin", orderId);

        OrderDetailsDto order = orderService.completeOrderAsAdmin(orderId);
        return ResponseEntity.ok(order);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
