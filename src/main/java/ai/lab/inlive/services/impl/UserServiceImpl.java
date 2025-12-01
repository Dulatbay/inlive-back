package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.request.UpdateUserProfileRequest;
import ai.lab.inlive.dto.response.UserResponse;
import ai.lab.inlive.entities.User;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.mappers.ImageMapper;
import ai.lab.inlive.mappers.UserMapper;
import ai.lab.inlive.repositories.UserRepository;
import ai.lab.inlive.security.keycloak.KeycloakBaseUser;
import ai.lab.inlive.security.keycloak.KeycloakRole;
import ai.lab.inlive.services.KeycloakService;
import ai.lab.inlive.services.UserService;
import ai.lab.inlivefilemanager.client.api.FileManagerApi;
import jakarta.persistence.EntityManager;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static ai.lab.inlive.constants.ValueConstants.FILE_MANAGER_USER_PHOTOS_DIR;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final EntityManager entityManager;
    private final MessageSource messageSource;
    private final UserMapper userMapper;
    private final ImageMapper imageMapper;
    private final FileManagerApi fileManagerApi;

    @Value("${spring.application.username}")
    private String keycloakUsername;

    @Value("${spring.application.password}")
    private String keycloakPassword;

    @Override
    public User getUserByKeycloakId(String keycloakId) {
        log.info("Retrieving user with keycloakId: {}", keycloakId);
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("error.user.notFound", null, LocaleContextHolder.getLocale())));

    }

    @Override
    @Transactional
    public void syncUsersBetweenDBAndKeycloak() {
        var usersFromDB = userRepository.findAll();

        var usersFromKeycloak = keycloakService.getAllUsers();

        log.info("usersFromKeycloak(with admin account and testers): {}, usersFromDB: {}", usersFromKeycloak.size(), usersFromDB.size());

        AtomicReference<String> testerUserId = new AtomicReference<>("");


        var usersToDeleteFromKeycloak = usersFromKeycloak.stream()
                .filter(user -> {
                            if (user.getEmail() != null && user.getEmail().equals(keycloakUsername)) {
                                testerUserId.set(user.getId());
                                return false;
                            }

                            return usersFromDB
                                    .stream()
                                    .noneMatch(user1 -> user1.getKeycloakId()
                                            .equals(user.getId()))
                                    &&
                                    !user.getUsername().equals(keycloakUsername);
                        }
                )
                .toList();

        for (var user : usersToDeleteFromKeycloak) {
            keycloakService.deleteUserById(user.getId());
        }

        AtomicBoolean testUserExists = new AtomicBoolean(false);

        List<User> usersToDeleteFromDB = usersFromDB
                .stream()
                .filter(user -> {
                    if (user.getKeycloakId().equals(testerUserId.get())) {
                        testUserExists.set(true);
                    }
                    return usersFromKeycloak
                            .stream()
                            .noneMatch(userRepresentation -> userRepresentation
                                    .getId()
                                    .equals(user.getKeycloakId()));
                })
                .toList();
        log.info("usersToDeleteFromKeycloak: {}, usersToDeleteFromDB: {}", usersToDeleteFromKeycloak.size(), usersToDeleteFromDB.size());

        if(!usersToDeleteFromDB.isEmpty()){
            userRepository.deleteAll(usersToDeleteFromDB);
            entityManager.flush();
            entityManager.clear();
        }


        log.info("Test user exists in db: {}, test user exists in keycloak: {}", testUserExists.get(), !testerUserId.get().isEmpty());

        log.info("Tester id: {}", testerUserId.get());

        if (!testUserExists.get()) {
            if (testerUserId.get().isEmpty()) {
                var keycloakUser = new KeycloakBaseUser();
                keycloakUser.setEmail(keycloakUsername);
                keycloakUser.setPassword(keycloakPassword);
                keycloakUser.setFirstName("Akhan");
                keycloakUser.setLastName("Dulatbay");
                var keycloakTester = keycloakService.createUserByRole(keycloakUser,
                        KeycloakRole.ADMIN
                );
                testerUserId.set(keycloakTester.getId());
            }

            var user = new User();
            user.setKeycloakId(testerUserId.get());
            user.setPhoneNumber("tester");
            user.setFirstName(keycloakUsername);
            user.setLastName("tester");

            userRepository.save(user);
            log.info("New tester created");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String keycloakId) {
        log.info("Fetching current user info for keycloakId: {}", keycloakId);

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        "User not found with Keycloak ID: " + keycloakId));

        log.info("Successfully fetched user info: {} {}", user.getFirstName(), user.getLastName());
        return userMapper.toDto(user, imageMapper);
    }

    @Override
    @Transactional
    public void updateUserProfile(String keycloakId, UpdateUserProfileRequest request) {
        log.info("Updating user profile for keycloakId: {}", keycloakId);

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        "User not found with Keycloak ID: " + keycloakId));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        
        userRepository.save(user);

        try {
            var userResource = keycloakService.getUserById(keycloakId);
            UserRepresentation keycloakUser = userResource.toRepresentation();
            keycloakUser.setFirstName(request.getFirstName());
            keycloakUser.setLastName(request.getLastName());
            keycloakUser.setEmail(request.getEmail());
            userResource.update(keycloakUser);
            
            log.info("Successfully updated user profile: {} {}", user.getFirstName(), user.getLastName());
        } catch (Exception e) {
            log.error("Failed to update user in Keycloak: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to update user in Keycloak: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateUserPhoto(String keycloakId, MultipartFile photo) {
        log.info("Updating profile photo for user with ID: {}", keycloakId);

        var user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(
                        HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        "User not found with ID: " + keycloakId
                ));

        if (photo != null && !photo.isEmpty()) {
            List<String> uploadedUrls = Objects.requireNonNull(
                    fileManagerApi
                            .uploadFiles(FILE_MANAGER_USER_PHOTOS_DIR, List.of(photo), true)
                            .getBody()
            );

            if (!uploadedUrls.isEmpty()) {
                user.setPhotoUrl(uploadedUrls.getFirst());
            }
        }

        userRepository.save(user);

        log.info("Successfully updated profile photo for user with ID: {}", keycloakId);
    }


    @Override
    @Transactional
    public void deleteUserPhoto(String keycloakId) {
        log.info("Deleting photo for user with keycloakId: {}", keycloakId);

        var user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        "User not found with Keycloak ID: " + keycloakId));

        if (user.getPhotoUrl() == null || user.getPhotoUrl().isEmpty()) {
            throw new IllegalArgumentException("User has no photo to delete");
        }

        deletePhotoFile(user.getPhotoUrl());

        user.setPhotoUrl(null);
        userRepository.save(user);

        log.info("Successfully deleted photo for user: {}", keycloakId);
    }

    private void deletePhotoFile(String filename) {
        try {
            fileManagerApi.deleteFile(FILE_MANAGER_USER_PHOTOS_DIR, filename);
            log.info("Deleted photo file from file-api: {}", filename);
        } catch (Exception e) {
            log.warn("Failed to delete photo file from file-api: {}", e.getMessage());
        }
    }
}
