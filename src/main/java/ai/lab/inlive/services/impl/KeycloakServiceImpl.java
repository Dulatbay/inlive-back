package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.request.UpdatePasswordRequest;
import ai.lab.inlive.dto.request.UserRegistrationRequest;
import ai.lab.inlive.dto.response.AuthResponse;
import ai.lab.inlive.security.keycloak.KeycloakBaseUser;
import ai.lab.inlive.security.keycloak.KeycloakError;
import ai.lab.inlive.security.keycloak.KeycloakRole;
import ai.lab.inlive.services.KeycloakService;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    private final RestTemplate restTemplate;

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

    private final MessageSource messageSource;


    @Override
    public UserRepresentation createUserByRole(KeycloakBaseUser sellerRegistrationRequest, KeycloakRole keycloakRole) {
        UserRepresentation userRepresentation = setupUserRepresentation(sellerRegistrationRequest);
        String userId = null;
        try (Response response = getUsersResource().create(userRepresentation)) {
            handleUnsuccessfulResponse(response);
            userId = CreatedResponseUtil.getCreatedId(response);
            UserResource userResource = setupUserResource(sellerRegistrationRequest, keycloakRole, userId);

            try {
                sendEmail(userId);
            } catch (Exception e) {
                log.error("Exception: ", e);
                throw new IllegalArgumentException(messageSource.getMessage("services-impl.keycloak-service-impl.invalid-email", null, LocaleContextHolder.getLocale()));
            }

            return userResource.toRepresentation();
        } catch (Exception e) {
            handleExceptionAfterUserIdCreated(userId);
            throw e;
        }
    }

    private UserRepresentation setupUserRepresentation(KeycloakBaseUser request) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setFirstName(request.getFirstName());
        userRepresentation.setLastName(request.getLastName());
        userRepresentation.setEmail(request.getEmail());
        userRepresentation.setUsername(request.getEmail());
        return userRepresentation;
    }

    private void handleUnsuccessfulResponse(Response response) {
        if (response.getStatus() != 201) {
            log.info("response status: {}", response.getStatus());
            log.info("response entity: {}", response.getEntity());
            if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                var object = response.readEntity(KeycloakError.class);
                throw new IllegalArgumentException(object.getErrorMessage());
            } else {
                throw new InternalServerErrorException(messageSource.getMessage("services-impl.keycloak-service-impl.unknown-error", null, LocaleContextHolder.getLocale()));
            }
        }
    }

    private UserResource setupUserResource(KeycloakBaseUser keycloakBaseUser, KeycloakRole keycloakRole, String userId) {
        UserResource userResource = getUsersResource().get(userId);
        userResource.resetPassword(getPasswordCredential(keycloakBaseUser.getPassword(), false));
        userResource.roles() //
                .clientLevel(getClient().getId())
                .add(Collections.singletonList(getClientRole(getClient(), keycloakRole)));
        return userResource;
    }

    private void handleExceptionAfterUserIdCreated(String userId) {
        if (userId != null) getUsersResource().delete(userId);
    }

    private CredentialRepresentation getPasswordCredential(String password, boolean temporary) {
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(temporary);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);
        return passwordCred;
    }

    private RoleRepresentation getClientRole(ClientRepresentation client, KeycloakRole keycloakRole) {
        return getRealmResource().clients().get(client.getId())
                .roles().get(keycloakRole.name()).toRepresentation();
    }


    private Keycloak getAdminKeycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.PASSWORD)
                .username(username)
                .password(password)
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10)
                        .build())
                .build();
    }


    private RealmResource getRealmResource() {
        return getAdminKeycloak().realm(realm);
    }

    private UsersResource getUsersResource() {
        return getRealmResource().users();
    }

    private ClientRepresentation getClient() {
        return getRealmResource().clients()
                .findByClientId(clientId).getFirst();
    }

    private void sendEmail(String userId) {
        getUsersResource().get(userId).sendVerifyEmail();
    }

    private Keycloak getKeycloak(String username, String password) {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.PASSWORD)
                .username(username)
                .password(password)
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10).build())
                .build();
    }

    @Override
    public AuthResponse getAuthResponse(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BadRequestException("Username or password is empty");
        }

        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("username", username);
        form.add("password", password);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String accessToken = (String) body.get("access_token");
                String refreshToken = (String) body.get("refresh_token");
                Integer expiresIn = (Integer) body.get("expires_in");
                Integer refreshExpIn = (Integer) body.get("refresh_expires_in");
                String tokenType = (String) body.get("token_type");

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .expiresIn(expiresIn != null ? expiresIn.longValue() : 3600L)
                        .refreshExpiresIn(refreshExpIn != null ? refreshExpIn.longValue() : null)
                        .tokenType(tokenType != null ? tokenType : "Bearer")
                        .build();
            } else {
                throw new RuntimeException("Failed to get auth response: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid username or password: " + e.getMessage());
        }
    }

    @Override
    public void deleteUserById(String userId) {
        log.info("Deleted user with id {}", userId);
        getUserById(userId).remove();
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        String logoutUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/logout";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(logoutUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("User logged out successfully");
            } else {
                log.error("Failed to logout user: {}", response.getStatusCode());
                throw new RuntimeException("Failed to logout user: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error during logout: ", e);
            throw new RuntimeException("Error during logout: " + e.getMessage());
        }
    }

    @Override
    public List<UserRepresentation> getAllUsers() {
        return getUsersResource().list();
    }

    @Override
    public List<UserRepresentation> getAllUsersByRole(KeycloakRole keycloakRole) {
        return getRealmResource()
                .clients()
                .get(getClient().getId())
                .roles().get(keycloakRole.name())
                .getUserMembers();
    }


    @Override
    public UserResource getUserById(String id) {
        return getUsersResource().get(id);
    }

    @Override
    public UserResource updateUser(KeycloakBaseUser keycloakBaseUser) {
        var usersResource = getUsersResource();
        List<UserRepresentation> users = usersResource.search(keycloakBaseUser.getEmail(), 0, 1);

        if (users.isEmpty()) {
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.keycloak-service-impl.user-not-found-by-email", null, LocaleContextHolder.getLocale()));
        }
        UserRepresentation userToUpdate = users.get(0);
        userToUpdate.setFirstName(keycloakBaseUser.getFirstName());
        userToUpdate.setLastName(keycloakBaseUser.getLastName());
        userToUpdate.setEmail(keycloakBaseUser.getEmail());

        UserResource userResource = usersResource.get(userToUpdate.getId());
        userResource.update(userToUpdate);

        log.info("Update user: {}", userResource);

        return userResource;
    }

    @Override
    public void updatePassword(String keycloakId, UpdatePasswordRequest updatePassword) {
        try {
            var keycloak = getKeycloak(updatePassword.getEmail(), updatePassword.getOldPassword());
            keycloak.tokenManager().getAccessTokenString();

            UserResource userResource = getUsersResource().get(keycloakId);

            CredentialRepresentation newPassword = new CredentialRepresentation();
            newPassword.setType(CredentialRepresentation.PASSWORD);
            newPassword.setValue(updatePassword.getNewPassword());
            newPassword.setTemporary(false);


            userResource.resetPassword(newPassword);
        } catch (NotAuthorizedException e) {
            log.error("Old password is incorrect for user with ID: {}", keycloakId);
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.keycloak-service-impl.incorrect-old-password", null, LocaleContextHolder.getLocale()));
        }
    }

    // end of Akhan's code

    @Override
    public AuthResponse registerUser(UserRegistrationRequest req) {
        validate(req);

        try (Keycloak adminKC = getAdminKeycloak()) {
            RealmResource realmResource = adminKC.realm(realm);
            UsersResource users = realmResource.users();

            // 1) Проверка существования по email/username (лучше хранить username = email)
            if (!users.search(req.email(), true).isEmpty()) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }

            // 2) Создание пользователя
            UserRepresentation user = new UserRepresentation();
            user.setUsername(req.email());
            user.setEmail(req.email());
            user.setFirstName(req.firstName());
            user.setLastName(req.lastName());
            user.setEnabled(true);
            user.setEmailVerified(false);
            user.setAttributes(Map.of("appRole", List.of(req.role())));

            Response createResp = users.create(user);
            if (createResp.getStatus() == 201) {
                String userId = CreatedResponseUtil.getCreatedId(createResp);
                log.info("User created in Keycloak, id={}", userId);

                UserResource userRes = users.get(userId);

                // 3) Установка пароля
                CredentialRepresentation passwordCred = new CredentialRepresentation();
                passwordCred.setType(CredentialRepresentation.PASSWORD);
                passwordCred.setTemporary(false);
                passwordCred.setValue(req.password());
                userRes.resetPassword(passwordCred);

                // 4) Назначение роли
                assignRole(realmResource, userRes, req.role());

                // (опционально) Отправка email-верификации:
                // userRes.sendVerifyEmail();

                // 5) Немедленный логин нового пользователя — получение токенов
                AuthResponse tokens = loginAsNewUser(req.email(), req.password());

                return tokens;
            } else if (createResp.getStatus() == 409) {
                throw new IllegalArgumentException("Пользователь уже существует (409 Conflict)");
            } else {
                String err = Optional.ofNullable(createResp.getEntity())
                        .map(Object::toString).orElse("unknown");
                log.error("Create user failed: status={}, body={}", createResp.getStatus(), err);
                throw new RuntimeException("Ошибка создания пользователя в Keycloak: " + createResp.getStatus());
            }
        } catch (Exception e) {
            log.error("registerUser failed", e);
            throw (e instanceof RuntimeException re) ? re : new RuntimeException(e);
        }
    }

    private void validate(UserRegistrationRequest r) {
        if (r == null
                || isBlank(r.email())
                || isBlank(r.password())
                || isBlank(r.firstName())
                || isBlank(r.lastName())
                || isBlank(r.role())) {
            throw new IllegalArgumentException("Invalid registration request");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private void assignRole(RealmResource realmResource, UserResource userRes, String roleName) {
        // Попытка найти realm-role
        Optional<RoleRepresentation> realmRoleOpt = realmResource.roles().list()
                .stream()
                .filter(r -> r.getName().equalsIgnoreCase(roleName))
                .findFirst();

        if (realmRoleOpt.isPresent()) {
            userRes.roles().realmLevel().add(List.of(realmRoleOpt.get()));
            log.info("Assigned realm role: {}", roleName);
            return;
        }

        // Если realm-role не нашли — пытаемся client-role по clientId
        ClientRepresentation client = realmResource.clients().findByClientId(clientId)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Client not found by clientId=" + clientId));

        var clientRoles = realmResource.clients().get(client.getId()).roles().list();
        var clientRoleOpt = clientRoles.stream()
                .filter(cr -> cr.getName().equalsIgnoreCase(roleName))
                .findFirst();

        if (clientRoleOpt.isPresent()) {
            userRes.roles().clientLevel(client.getId()).add(List.of(clientRoleOpt.get()));
            log.info("Assigned client role: {} for client={}", roleName, clientId);
        } else {
            log.warn("Role {} not found as realm or client role — пропускаю назначение", roleName);
        }
    }

    private AuthResponse loginAsNewUser(String userEmail, String userPassword) {
        // Если клиент конфиденциальный — используй clientSecret; для public-client secret не нужен
        try (var kc = KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret) // если public-client — удали эту строку
                .username(userEmail)
                .password(userPassword)
                .grantType(OAuth2Constants.PASSWORD)
                .build()) {

            var token = kc.tokenManager().getAccessToken();
            return new AuthResponse(
                    token.getToken(),
                    token.getRefreshToken(),
                    token.getExpiresIn(),
                    token.getRefreshExpiresIn(),
                    token.getTokenType()
            );
        }
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadRequestException("Refresh token is empty");
        }

        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();

                String accessToken = (String) body.get("access_token");
                String newRefreshToken = (String) body.get("refresh_token");
                Integer expiresIn = (Integer) body.get("expires_in");
                Integer refreshExpIn = (Integer) body.get("refresh_expires_in");
                String tokenType = (String) body.get("token_type");

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(newRefreshToken != null ? newRefreshToken : refreshToken)
                        .expiresIn(expiresIn != null ? expiresIn.longValue() : 3600L)
                        .refreshExpiresIn(refreshExpIn != null ? refreshExpIn.longValue() : null)
                        .tokenType(tokenType != null ? tokenType : "Bearer")
                        .build();
            } else {
                throw new RuntimeException("Ошибка обновления токена: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Неверный или истекший refresh token: " + e.getMessage());
        }
    }
}
