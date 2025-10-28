package com.example.product_service.controller;

import com.example.product_service.dto.ErrorResponseDto;
import com.example.product_service.dto.ProductDetailsDto;
import com.example.product_service.dto.ProductRequestDto;
import com.example.product_service.dto.ProductSummaryDto;
import com.example.product_service.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
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
                  "timestamp": "2025-10-27T14:55:00",
                  "path": "/api/products"
                }
                """
                        )
                )
        )
})
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(
            summary = "Get all products",
            description = "Fetches a list of all available products.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of products retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ProductSummaryDto.class)),
                                    examples = @ExampleObject(value = """
                                    [
                                        {
                                            "id": 1,
                                            "name": "Laptop",
                                            "price": 1999.99,
                                            "stock": 15
                                        },
                                        {
                                            "id": 2,
                                            "name": "Mouse",
                                            "price": 49.99,
                                            "stock": 200
                                        }
                                    ]
                                    """)
                            )
                    )
            }
    )
    public ResponseEntity<List<ProductSummaryDto>> getProducts() {
        log.info("GET /api/products - Fetching all products");
        List<ProductSummaryDto> products = productService.getProducts();
        log.debug("Fetched {} products", products.size());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by ID",
            description = "Fetches detailed information about a specific product by its ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Product details retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProductDetailsDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "id": 1,
                                        "name": "Laptop",
                                        "description": "High-end gaming laptop",
                                        "price": 1999.99,
                                        "stock": 15,
                                        "createdAt": "2025-10-27T14:00:00",
                                        "updatedAt": "2025-10-27T14:10:00"
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
                              "path": "/api/products/999"
                            }
                            """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<ProductDetailsDto> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{} - Fetching product details", id);
        ProductDetailsDto product = productService.getProductById(id);
        log.debug("Fetched product details: {}", product);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    @Operation(
            summary = "Create a new product (Admin only)",
            description = "Creates a new product using the provided product details.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product registration details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductRequestDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "name": "Laptop",
                                        "description": "High-end gaming laptop",
                                        "price": 1999.99,
                                        "stock": 15
                                    }
                                    """)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Product created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProductDetailsDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "id": 1,
                                        "name": "Laptop",
                                        "description": "High-end gaming laptop",
                                        "price": 1999.99,
                                        "stock": 15,
                                        "createdAt": "2025-10-27T14:00:00",
                                        "updatedAt": "2025-10-27T14:10:00"
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation failed for one or more fields",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                      "statusCode": 400,
                                      "message": "One or more fields have invalid values",
                                      "timestamp": "2025-10-27T21:29:07.283468",
                                      "path": "/api/products",
                                      "errors": {
                                        "price": "Price cannot be negative",
                                        "name": "A product must have a name",
                                        "stock": "Stock cannot be negative"
                                      }
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access denied",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(
                                            name = "Access Denied Response",
                                            value = """
                            {
                              "statusCode": 403,
                              "message": "Access denied: You do not have the required privileges to access this resource.",
                              "timestamp": "2025-10-27T14:55:00",
                              "path": "/api/products"
                            }
                            """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<ProductDetailsDto> createProduct(@Valid @RequestBody ProductRequestDto product) {
        log.info("POST /api/products - Creating product with name: {}", product.name());
        ProductDetailsDto created = productService.createProduct(product);
        log.info("Created product with ID: {}", created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing product (Admin only)",
            description = "Updates an existing product using the provided product details.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product update details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductRequestDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "name": "Laptop",
                                        "description": "Updated description",
                                        "price": 3000.00,
                                        "stock": 10
                                    }
                                    """)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Product updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProductDetailsDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "id": 1,
                                        "name": "Laptop",
                                        "description": "High-end gaming laptop",
                                        "price": 3000.00,
                                        "stock": 15,
                                        "createdAt": "2025-10-27T14:00:00",
                                        "updatedAt": "2025-10-27T14:10:00"
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation failed for one or more fields",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(value = """
                                    {
                                      "statusCode": 400,
                                      "message": "One or more fields have invalid values",
                                      "timestamp": "2025-10-27T21:29:07.283468",
                                      "path": "/api/products/1",
                                      "errors": {
                                        "price": "Price cannot be negative",
                                        "name": "A product must have a name",
                                        "stock": "Stock cannot be negative"
                                      }
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access denied",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(
                                            name = "Access Denied Response",
                                            value = """
                            {
                              "statusCode": 403,
                              "message": "Access denied: You do not have the required privileges to access this resource.",
                              "timestamp": "2025-10-27T14:55:00",
                              "path": "/api/products/1"
                            }
                            """
                                    )
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
                              "path": "/api/products/999"
                            }
                            """
                                    )
                            )

                    ),
            }
    )
    public ResponseEntity<ProductDetailsDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDto product
    ) {
        log.info("PUT /api/products/{} - Updating product", id);
        ProductDetailsDto updated = productService.updateProduct(id, product);
        log.info("Updated product ID: {}", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a product (Admin only)",
            description = "Deletes a product by its ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Product deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access denied",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class),
                                    examples = @ExampleObject(
                                            name = "Access Denied Response",
                                            value = """
                            {
                              "statusCode": 403,
                              "message": "Access denied: You do not have the required privileges to access this resource.",
                              "timestamp": "2025-10-27T14:55:00",
                              "path": "/api/products/1"
                            }
                            """
                                    )
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
                              "path": "/api/products/999"
                            }
                            """
                                    )
                            )

                    )
            }
    )
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/products/{} - Deleting product", id);
        productService.deleteProduct(id);
        log.info("Deleted product ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
