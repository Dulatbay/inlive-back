package ai.lab.inlivefilemanager.client.service;

import ai.lab.inlivefilemanager.client.dto.KeycloakTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;

@Service
public class KeycloakAuthService {

    @Value("${spring.application.keycloak-url}")
    private String authServerUrl;

    @Value("${spring.application.realm}")
    private String realm;

    @Value("${spring.application.client-id}")
    private String clientId;

    @Value("${spring.application.client-secret}")
    private String clientSecret;

    @Value("${spring.application.username}")
    private String username;

    @Value("${spring.application.password}")
    private String password;

    private String accessToken;

    private Instant tokenExpiration = Instant.now();  // Добавление времени истечения токена


    public String getAccessToken() {
        if (accessToken == null || tokenExpired()) {
            refreshToken();
        }
        return accessToken;
    }

    private boolean tokenExpired() {
        return Instant.now().isAfter(tokenExpiration);
    }

    private void refreshToken() {
        String url = String.format("%s/realms/%s/protocol/openid-connect/token", authServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // Build request body with client_secret if available
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("grant_type=password")
                .append("&client_id=").append(clientId)
                .append("&username=").append(username)
                .append("&password=").append(password);

        if (clientSecret != null && !clientSecret.isEmpty()) {
            requestBody.append("&client_secret=").append(clientSecret);
        }

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<KeycloakTokenResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, request, KeycloakTokenResponse.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            this.accessToken = response.getBody().getAccessToken();
            long expiresIn = response.getBody().getExpiresIn();
            tokenExpiration = Instant.now().plusSeconds(expiresIn - 30);
        } else {
            throw new RuntimeException("Failed to retrieve access token from Keycloak");
        }
    }
}
