package ai.lab.inlive.security.keycloak;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KeycloakBaseUser {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
}