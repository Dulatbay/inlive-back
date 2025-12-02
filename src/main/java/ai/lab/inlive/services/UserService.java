package ai.lab.inlive.services;

import ai.lab.inlive.dto.request.UpdateUserProfileRequest;
import ai.lab.inlive.dto.response.UserResponse;
import ai.lab.inlive.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    User getUserByKeycloakId(String keycloakId);

    @Transactional
    void syncUsersBetweenDBAndKeycloak();

    UserResponse getCurrentUser(String keycloakId);

    void updateUserProfile(String keycloakId, UpdateUserProfileRequest request);

    void updateUserPhoto(String keycloakId, MultipartFile photo);

    void deleteUserPhoto(String keycloakId);
}