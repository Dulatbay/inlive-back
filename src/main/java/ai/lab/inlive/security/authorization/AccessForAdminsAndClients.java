package ai.lab.inlive.security.authorization;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@PreAuthorize("hasAuthority(T(ai.lab.inlive.security.keycloak.KeycloakRole).ADMIN) or " +
        "hasAuthority(T(ai.lab.inlive.security.keycloak.KeycloakRole).CLIENT)")
public @interface AccessForAdminsAndClients {
}