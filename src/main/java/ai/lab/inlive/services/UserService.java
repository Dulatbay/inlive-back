package ai.lab.inlive.services;

import ai.lab.inlive.entities.User;
import jakarta.transaction.Transactional;

public interface UserService {
    User getUserByKeycloakId(String keycloakId);

    @Transactional
    void syncUsersBetweenDBAndKeycloak();
}