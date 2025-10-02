package ai.lab.inlive.config;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {
    @Value("${spring.application.realm}")
    private String realm;

    @Value("${spring.application.client-id}")
    private String clientId;

    @Value("${spring.application.keycloak-url}")
    private String keycloakUrl;

    @Value("${spring.application.username}")
    private String username;

    @Value("${spring.application.password}")
    private String password;

    @Value("${spring.application.client-secret}")
    private String clientSecret;

    @Bean
    public Keycloak adminKeycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(realm)
                .clientId(clientId)
                .grantType(OAuth2Constants.PASSWORD)
                .clientSecret(clientSecret)
                .username(username)
                .password(password)
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10)
                        .build())
                .build();
    }

    @Bean
    public KeycloakUserClientFactory keycloakUserClientFactory() {
        return new KeycloakUserClientFactory(keycloakUrl, realm, clientId, clientSecret);
    }

    public static class KeycloakUserClientFactory {
        private final String keycloakUrl;
        private final String realm;
        private final String clientId;
        private final String clientSecret;

        public KeycloakUserClientFactory(String keycloakUrl, String realm, String clientId, String clientSecret) {
            this.keycloakUrl = keycloakUrl;
            this.realm = realm;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
        }

        public Keycloak createUserKeycloak(String username, String password) {
            return KeycloakBuilder.builder()
                    .serverUrl(keycloakUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .grantType(OAuth2Constants.PASSWORD)
                    .clientSecret(clientSecret)
                    .username(username)
                    .password(password)
                    .resteasyClient(new ResteasyClientBuilderImpl()
                            .connectionPoolSize(10)
                            .build())
                    .build();
        }
    }
}