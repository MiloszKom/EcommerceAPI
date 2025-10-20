package com.example.user_service.service.client;

import com.example.user_service.dto.KeycloakUserRepresentation;
import com.example.user_service.dto.TokenResponse;
import com.example.user_service.exception.KeycloakException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
public class KeycloakClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${keycloak.server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin-client-id}")
    private String clientId;

    @Value("${keycloak.admin-client-secret}")
    private String clientSecret;

    public KeycloakClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable(value = "adminTokens", key = "'admin'")
    public String getAdminAccessToken() {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(tokenUrl, entity, TokenResponse.class);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new KeycloakException("Failed to get admin token: " + response.getStatusCode(), HttpStatus.UNAUTHORIZED);
        }
        return response.getBody().getAccessToken();
    }

    public String createUser(KeycloakUserRepresentation userRep) {
        String usersUrl = keycloakUrl + "/admin/realms/" + realm + "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAdminAccessToken());

        HttpEntity<KeycloakUserRepresentation> entity = new HttpEntity<>(userRep, headers);

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(usersUrl, entity, Void.class);

            URI location = response.getHeaders().getLocation();
            if (location == null) {
                throw new KeycloakException("No location header in successful user creation response", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            String userId = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);
            return userId;
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            HttpStatus status = (HttpStatus) e.getStatusCode();

            String message = parseKeycloakError(errorBody, "User creation failed");

            throw new KeycloakException(message, status);
        } catch (Exception e) {
            throw new KeycloakException("Failed to create user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public TokenResponse getUserTokens(String grantType, String username, String password, String scope) {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", grantType);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", username);
        body.add("password", password);
        if (scope != null) body.add("scope", scope);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(tokenUrl, entity, TokenResponse.class);
            if (response.getBody() == null) {
                throw new KeycloakException("No token in response", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return response.getBody();
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            HttpStatus status = (HttpStatus) e.getStatusCode();

            String message = parseKeycloakError(errorBody, "Invalid authentication request");

            throw new KeycloakException(message, status);
        } catch (Exception e) {
            throw new KeycloakException("Unexpected error during token request: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String parseKeycloakError(String body, String fallbackMessage) {
        if (body == null || body.trim().isEmpty()) {
            return fallbackMessage;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(body);
            JsonNode errorDesc = jsonNode.get("error_description");
            if (errorDesc != null && !errorDesc.isNull()) {
                return errorDesc.asText();
            }
            JsonNode errorMsg = jsonNode.get("errorMessage");
            if (errorMsg != null && !errorMsg.isNull()) {
                return errorMsg.asText();
            }
            JsonNode error = jsonNode.get("error");
            if (error != null && !error.isNull()) {
                return error.asText();
            }
        } catch (Exception parseEx) {
            System.out.println("Failed to parse Keycloak error: " + parseEx.getMessage());
        }
        return fallbackMessage + ": " + body;
    }
}