package ai.lab.inlive.security.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KeycloakBaseUser {
    // todo: add current validators for request to keycloak, because keycloak strictly return bad request if any field is null or not correct(ex: email must include @)
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    @JsonIgnore
    private String keycloakId;
}