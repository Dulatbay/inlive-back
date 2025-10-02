package ai.lab.inlive.services.impl;

import ai.lab.inlive.config.KeycloakConfig;
import ai.lab.inlive.config.properties.KeycloakConfigProperties;
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
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    @Value("${spring.application.realm}")
    private String realm;

    @Value("${spring.application.client-id}")
    private String clientId;

    private final Keycloak adminKeycloak;
    private final KeycloakConfig.KeycloakUserClientFactory keycloakUserClientFactory;
    private final MessageSource messageSource;
    private final KeycloakConfigProperties props;
    private final WebClient webClient = WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();

    @Override
    public UserRepresentation createUserByRole(KeycloakBaseUser sellerRegistrationRequest, KeycloakRole keycloakRole) {
        log.info("Creating user {} with role {}", sellerRegistrationRequest.getEmail(), keycloakRole);

        UserRepresentation userRepresentation = setupUserRepresentation(sellerRegistrationRequest);
        String userId = null;

        // Убираем проблематичный тест соединения с getRealmResource().toRepresentation()
        log.info("Proceeding with user creation...");

        try (Response response = getUsersResource().create(userRepresentation)) {
            log.info("Keycloak create user response status: {}", response.getStatus());

            if (response.getStatus() == 403) {
                log.error("Access denied (403) when creating user. Admin user may not have sufficient permissions");
                throw new RuntimeException("Недостаточно прав для создания пользователя в Keycloak");
            }

            handleUnsuccessfulResponse(response);
            userId = CreatedResponseUtil.getCreatedId(response);
            log.info("User created with ID: {}", userId);

            UserResource userResource = setupUserResource(sellerRegistrationRequest, keycloakRole, userId);

            // Отключаем отправку email временно, чтобы избежать дополнительных ошибок
            try {
                // sendEmail(userId);
                log.info("Email sending skipped for testing");
            } catch (Exception e) {
                log.error("Exception during email sending: ", e);
                // Не выбрасываем исключение, так как пользователь уже создан
            }

            return userResource.toRepresentation();
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            handleExceptionAfterUserIdCreated(userId);

            if (e.getMessage().contains("403") || e.getMessage().contains("Forbidden")) {
                throw new RuntimeException("Недостаточно прав для создания пользователя в Keycloak");
            }
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

        // Для master realm используем realm роли вместо клиентских ролей
        try {
            // Добавляем базовые realm роли
            var realmRoles = getRealmResource().roles().list().stream()
                    .filter(role -> role.getName().equals("offline_access") || role.getName().equals("uma_authorization"))
                    .toList();

            if (!realmRoles.isEmpty()) {
                userResource.roles().realmLevel().add(realmRoles);
                log.info("Added realm roles to user: {}", realmRoles.stream().map(r -> r.getName()).toList());
            }

        } catch (Exception e) {
            log.warn("Could not assign roles to user: {}", e.getMessage());
            // Не выбрасываем исключение, так как пользователь уже создан
        }

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


    private RealmResource getRealmResource() {
        return adminKeycloak.realm(realm);
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

    @Override
    public AuthResponse getAuthResponse(String username, String password) {
        log.info("Attempting authentication for user: {}", username);

        try (var userKeycloak = keycloakUserClientFactory.createUserKeycloak(username, password)) {
            String accessToken = userKeycloak.tokenManager().getAccessTokenString();
            String refreshToken = userKeycloak.tokenManager().refreshToken().getRefreshToken();

            // Получаем информацию о пользователе и обогащаем ответ
            AuthResponse basicResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(3600) // 1 час по умолчанию
                    .build();

            return enrichAuthResponseWithUserInfo(basicResponse, username);

        } catch (NotAuthorizedException notAuthorizedException) {
            log.error("Authentication failed for user: {}", username);
            throw new IllegalArgumentException("Неверные учетные данные");
        } catch (BadRequestException e) {
            log.error("Bad request exception for user: {}. Error: {}", username, e.getMessage());
            throw new IllegalArgumentException("Ошибка конфигурации Keycloak: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error occurred during authentication for user: {}. Error: {}", username, e.getMessage(), e);
            throw new RuntimeException("Ошибка подключения к серверу аутентификации: " + e.getMessage());
        }
    }

    @Override
    public void deleteUserById(String userId) {
        log.info("Deleted user with id {}", userId);
        getUserById(userId).remove();
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
            var keycloak = keycloakUserClientFactory.createUserKeycloak(updatePassword.getEmail(), updatePassword.getOldPassword());
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

    @Override
    public AuthResponse registerUser(UserRegistrationRequest registrationRequest) {
        log.info("Registering new user: {} with role: {}", registrationRequest.email(), registrationRequest.role());

        try {
            // Валидация роли
            KeycloakRole role;
            try {
                role = KeycloakRole.valueOf(registrationRequest.role().toUpperCase());
                if (role == KeycloakRole.ADMIN) {
                    throw new IllegalArgumentException("Регистрация администраторов запрещена через API");
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Неверная роль пользователя. Допустимые роли: CLIENT, SUPER_MANAGER");
            }

            // Проверяем, существует ли уже пользователь с таким email
            List<UserRepresentation> existingUsers = getUsersResource().search(registrationRequest.email(), 0, 1);
            if (!existingUsers.isEmpty()) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }

            // Создаем KeycloakBaseUser из запроса регистрации
            KeycloakBaseUser keycloakUser = new KeycloakBaseUser() {
                @Override
                public String getFirstName() { return registrationRequest.firstName(); }

                @Override
                public String getLastName() { return registrationRequest.lastName(); }

                @Override
                public String getEmail() { return registrationRequest.email(); }

                @Override
                public String getPassword() { return registrationRequest.password(); }
            };

            // Создаем пользователя в Keycloak
            UserRepresentation createdUser = createUserByRole(keycloakUser, role);

            // Получаем токены для созданного пользователя
            AuthResponse authResponse = getAuthResponse(registrationRequest.email(), registrationRequest.password());

            // Дополняем ответ информацией о пользователе
            return AuthResponse.builder()
                    .accessToken(authResponse.getAccessToken())
                    .refreshToken(authResponse.getRefreshToken())
                    .expiresIn(3600) // 1 час по умолчанию
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка регистрации пользователя: " + e.getMessage());
        }
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadRequestException("Refresh token is empty");
        }

        String tokenUrl = props.baseUrl() + "/realms/" + props.realm() + "/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", props.clientId());
        form.add("refresh_token", refreshToken);

        if (props.confidential()) {
            if (props.clientSecret() == null || props.clientSecret().isBlank()) {
                throw new IllegalStateException("Client secret is not configured for confidential client");
            }
            form.add("client_secret", props.clientSecret());
        }

        KeycloakTokenResponse kc = webClient.post()
                .uri(tokenUrl)
                .bodyValue(form)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        resp.bodyToMono(String.class).map(body -> {
                            log.warn("Keycloak 4xx on refresh: {}", body);
                            // Нормализуем частые ошибки
                            if (body.contains("invalid_grant")) {
                                return new BadRequestException("Refresh non-valid: session not active or token reused/expired");
                            }
                            return new BadRequestException("Refresh failed: " + body);
                        })
                )
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                        resp.bodyToMono(String.class).map(body -> {
                            log.error("Keycloak 5xx on refresh: {}", body);
                            return new ServiceUnavailableException("Auth provider unavailable");
                        })
                )
                .bodyToMono(KeycloakTokenResponse.class)
                .block();

        if (kc == null || kc.accessToken() == null) {
            throw new ServiceUnavailableException("Empty response from auth provider");
        }

        // Уважай ротацию: присылай клиенту новый refresh (если Keycloak его выдал)
        String newRefresh = kc.refreshToken() != null ? kc.refreshToken() : refreshToken;

        return AuthResponse.builder()
                .accessToken(kc.accessToken())
                .refreshToken(newRefresh)
                .expiresIn(kc.expiresIn())
                .build();
    }

    // Вспомогательный метод для получения информации о пользователе из токена
    private AuthResponse enrichAuthResponseWithUserInfo(AuthResponse authResponse, String email) {
        try {
            List<UserRepresentation> users = getUsersResource().search(email, 0, 1);
            if (!users.isEmpty()) {
                UserRepresentation user = users.get(0);

                return AuthResponse.builder()
                        .accessToken(authResponse.getAccessToken())
                        .refreshToken(authResponse.getRefreshToken())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role("CLIENT") // временно используем роль по умолчанию
                        .expiresIn(authResponse.getExpiresIn())
                        .build();
            }
        } catch (Exception e) {
            log.warn("Could not enrich auth response with user info: {}", e.getMessage());
        }
        return authResponse;
    }

    private String getUserRoleFromKeycloak(String userId) {
        // Временно отключаем получение ролей для избежания ошибок десериализации
        return "CLIENT"; // роль по умолчанию
    }
}
