package ai.lab.inlive.mappers;

import ai.lab.inlive.entities.User;
import ai.lab.inlive.entities.Accommodation;
import ai.lab.inlive.entities.AccommodationUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static ai.lab.inlive.constants.ValueConstants.FILE_MANAGER_ACCOMMODATION_IMAGE_DIR;
import static ai.lab.inlive.constants.ValueConstants.FILE_MANAGER_ACCOMMODATION_UNIT_IMAGE_DIR;
import static ai.lab.inlive.constants.ValueConstants.FILE_MANAGER_USER_PHOTOS_DIR;

@Component
public class ImageMapper {
    @Value("${spring.application.file-api.url}")
    private String fileApiUrl;

    public Set<String> getPathToAccommodationImages(Accommodation accommodation) {
        return accommodation.getImages().stream()
                .map(image -> fileApiUrl + "/" + FILE_MANAGER_ACCOMMODATION_IMAGE_DIR + "/retrieve/files/" + image.getImageUrl())
                .collect(Collectors.toSet());
    }

    public Set<String> getPathToAccommodationUnitImages(AccommodationUnit accommodationUnit) {
        return accommodationUnit.getImages().stream()
                .map(image -> fileApiUrl + "/" + FILE_MANAGER_ACCOMMODATION_UNIT_IMAGE_DIR + "/retrieve/files/" + image.getImageUrl())
                .collect(Collectors.toSet());
    }

    public String getPathToUserPhoto(User user) {
        if (user.getPhotoUrl() == null || user.getPhotoUrl().isEmpty()) {
            return null;
        }
        return fileApiUrl + "/" + FILE_MANAGER_USER_PHOTOS_DIR + "/retrieve/files/" + user.getPhotoUrl();
    }
}
