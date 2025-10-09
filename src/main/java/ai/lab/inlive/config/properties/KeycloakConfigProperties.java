package ai.lab.inlive.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class KeycloakConfigProperties {
    @Value("${spring.application.realm}")
    private String realm;

    @Value("${spring.application.client-id}")
    private String clientId;

    @Value("${spring.application.client-secret}")
    private String clientSecret;

    @Value("${spring.application.keycloak-url}")
    private String keycloakUrl;

    @Value("${spring.application.username}")
    private String adminUsername;

    @Value("${spring.application.password}")
    private String adminPassword;

    @Value("${spring.application.send-email}")
    private boolean sendEmail;
}
