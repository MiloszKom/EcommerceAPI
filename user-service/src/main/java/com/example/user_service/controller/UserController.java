    package com.example.user_service.controller;

    import com.example.user_service.dto.*;
    import com.example.user_service.service.IUserService;
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.media.Content;
    import io.swagger.v3.oas.annotations.media.ExampleObject;
    import io.swagger.v3.oas.annotations.media.Schema;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    import io.swagger.v3.oas.annotations.responses.ApiResponses;
    import org.springframework.security.oauth2.jwt.Jwt;
    import jakarta.validation.Valid;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.validation.annotation.Validated;
    import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/api/users")
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
                  "path": "/api/users/me"
                }
                """
                            )
                    )
            )
    })
    public class UserController {
        private static final Logger log = LoggerFactory.getLogger(UserController.class);
        private final IUserService userService;

        public UserController(IUserService userService) {
            this.userService = userService;
        }

        @GetMapping("/me")
        @Operation(
                summary = "Get current user details",
                description = """
            Fetches details of the currently authenticated user using JWT claims.
            The user record will be automatically created if it does not exist in the database.
            """,
                responses = {
                        @ApiResponse(
                                responseCode = "200",
                                description = "User details successfully retrieved",
                                content = @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = UserDetailsResponseDto.class),
                                        examples = @ExampleObject(
                                                name = "Successful Response",
                                                value = """
                            {
                              "username": "johndoe",
                              "email": "john.doe@example.com",
                              "address": "123 Main St, Springfield",
                              "phoneNumber": "+14155552671",
                              "createdAt": "2025-01-15T09:20:30",
                              "updatedAt": "2025-02-10T14:42:10"
                            }
                            """
                                        )
                                )
                        )
                }
        )
        public ResponseEntity<UserDetailsResponseDto> getCurrentUserDetails(@AuthenticationPrincipal Jwt jwt) {
            String userId = jwt.getSubject();
            String username = jwt.getClaimAsString("preferred_username");
            String email = jwt.getClaimAsString("email");

            log.debug("Fetching current user details for userId: {}", userId);

            UserDetailsResponseDto details = userService.getCurrentUserDetails(userId, username, email);
            return ResponseEntity.ok(details);
        }

        @PutMapping("/me")
        @Operation(
                summary = "Update current user details",
                description = """
            Updates the address and phone number of the currently authenticated user.
            The user record will be automatically created if it does not exist in the database.
            """,
                responses = {
                        @ApiResponse(
                                responseCode = "200",
                                description = "User details successfully updated",
                                content = @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = UserDetailsResponseDto.class),
                                        examples = @ExampleObject(
                                                name = "Successful Update Response",
                                                value = """
                            {
                              "username": "johndoe",
                              "email": "john.doe@example.com",
                              "address": "456 Elm St, Springfield",
                              "phoneNumber": "+14155559876",
                              "createdAt": "2025-01-15T09:20:30",
                              "updatedAt": "2025-10-27T14:50:00"
                            }
                            """
                                        )
                                )
                        ),
                        @ApiResponse(
                                responseCode = "400",
                                description = "Validation failed for one or more fields",
                                content = @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseDto.class),
                                        examples = @ExampleObject(
                                                name = "Validation Error Response",
                                                value = """
                            {
                              "statusCode": 400,
                              "message": "Validation failed",
                              "timestamp": "2025-10-27T14:52:30",
                              "path": "/api/users/me",
                              "errors": {
                                "phoneNumber": "Invalid phone number format",
                                "address": "Address must not exceed 255 characters"
                              }
                            }
                            """
                                        )
                                )
                        )
                }
        )
        public ResponseEntity<UserDetailsResponseDto> updateCurrentUserDetails(
                @AuthenticationPrincipal Jwt jwt,
                @Valid @RequestBody UserUpdateRequestDto updateDto
        ) {
            String userId = jwt.getSubject();
            String username = jwt.getClaimAsString("preferred_username");
            String email = jwt.getClaimAsString("email");

            log.debug("Updating details for userId: {}", userId);

            UserDetailsResponseDto updatedDetails = userService.updateUserDetails(userId, username, email, updateDto);
            return ResponseEntity.ok(updatedDetails);
        }
    }
