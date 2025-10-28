package com.example.cart_service.controller;

import com.example.cart_service.dto.CartRequest;
import com.example.cart_service.dto.CartDto;
import com.example.cart_service.dto.ErrorResponseDto;
import com.example.cart_service.dto.UpdateCartItemRequest;
import com.example.cart_service.service.ICartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
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
                  "path": "/api/cart"
                }
                """
                        )
                )
        )
})
public class CartController {
    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final ICartService cartService;

    public CartController(ICartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(
            summary = "Get current user's cart",
            description = "Fetches the shopping cart for the current user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cart retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CartDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                      "cartId": 1,
                                      "userId": "345c8ef4-e13f-4848-b770-9a5267965d62",
                                      "items": [
                                        {
                                          "id": 1,
                                          "productId": 101,
                                          "productName": "Laptop",
                                          "quantity": 2,
                                          "price": 1999.99
                                        },
                                        {
                                          "id": 2,
                                          "productId": 102,
                                          "productName": "Mouse",
                                          "quantity": 1,
                                          "price": 49.99
                                        }
                                      ]
                                    }
                                    """)
                            )
                    )
            }
    )
    public ResponseEntity<CartDto> getUserCart(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.debug("Fetching cart for userId={}", userId);
        return ResponseEntity.ok(cartService.getUserCart(userId));
    }

    @PostMapping("/add")
    @Operation(
            summary = "Add product to cart",
            description = "Adds a product to the current user's shopping cart.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product ID and quantity to add",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CartRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "productId": 1,
                                        "quantity": 5
                                    }
                                    """)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Product added to cart successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CartDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                      "cartId": 1,
                                      "userId": "345c8ef4-e13f-4848-b770-9a5267965d62",
                                      "items": [
                                        {
                                          "itemId": 30,
                                          "productId": 1,
                                          "quantity": 5
                                        }
                                      ]
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation failed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "statusCode": 400,
                                        "message": "One or more fields have invalid values",
                                        "timestamp": "2025-10-27T22:09:46.301708",
                                        "path": "/api/cart/add",
                                        "errors": {
                                          "quantity": "Quantity must be at least 1"
                                        }
                                      }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Product not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(
                                            name = "Not Found",
                                            value = """
                            {
                              "statusCode": 404,
                              "message": "Product not found with the given input data productId: 999",
                              "timestamp": "2025-10-27T14:55:00",
                              "path": "/api/cart/add"
                            }
                            """     )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict - Product already exists in cart",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                       "statusCode": 409,
                                       "message": "Product already in cart",
                                       "timestamp": "2025-10-27T22:10:14.8556569",
                                       "path": "/api/cart/add"
                                     }
                                    """)
                            )
                    )
            }
    )
    public ResponseEntity<CartDto> addProductToCart(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CartRequest cartRequest
    ) {
        String userId = jwt.getSubject();
        log.info("UserId={} adding productId={} (quantity={})", userId, cartRequest.productId(), cartRequest.quantity());
        return ResponseEntity.ok(cartService.addProductToCart(userId, cartRequest));
    }

    @PutMapping("/{itemId}")
    @Operation(
            summary = "Update cart item",
            description = "Updates the quantity of a product in the current user's cart.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New quantity",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateCartItemRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "quantity": 10
                                    }
                                    """)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cart item updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CartDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                      "cartId": 5,
                                      "userId": "345c8ef4-e13f-4848-b770-9a5267965d62",
                                      "items": [
                                        {
                                          "itemId": 32,
                                          "productId": 9,
                                          "quantity": 1
                                        }
                                      ]
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation failed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "statusCode": 400,
                                        "message": "One or more fields have invalid values",
                                        "timestamp": "2025-10-27T22:09:46.301708",
                                        "path": "/api/cart/12",
                                        "errors": {
                                          "quantity": "Quantity must be at least 1"
                                        }
                                      }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Cart item not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "statusCode": 404,
                                        "message": "CartItem not found with the given input data id: 999",
                                        "timestamp": "2025-10-27T22:21:08.246851",
                                        "path": "/api/cart/999"
                                    }
                                    """)
                            )
                    )
            }
    )
    public ResponseEntity<CartDto> updateCartItem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateCartItemRequest updateRequest,
            @PathVariable Long itemId
    ) {
        String userId = jwt.getSubject();
        log.info("UserId={} updating productId={} (new quantity={})", userId, itemId, updateRequest.quantity());
        return ResponseEntity.ok(cartService.updateCartItem(userId, itemId, updateRequest));
    }

    @DeleteMapping("/{itemId}")
    @Operation(
            summary = "Remove a product from cart",
            description = "Removes a specific product from the current user's shopping cart.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Cart item removed successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Cart item not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                      "statusCode": 404,
                                      "message": "Cart item not found with the given id: 999",
                                      "timestamp": "2025-10-27T15:25:00",
                                      "path": "/api/cart/999"
                                    }
                                    """)
                            )
                    )
            }
    )
    public ResponseEntity<Void> removeCartItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long itemId
    ) {
        String userId = jwt.getSubject();
        log.info("UserId={} removing productId={} from cart", userId, itemId);
        cartService.removeCartItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    @Operation(
            summary = "Clear cart",
            description = "Removes all items from the current user's shopping cart.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Cart cleared successfully"
                    )
            }
    )
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.warn("UserId={} clearing cart", userId);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
