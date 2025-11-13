package ai.lab.inlive.mappers;

import ai.lab.inlive.entities.Accommodation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static ai.lab.inlive.constants.ValueConstants.FILE_MANAGER_ACCOMMODATION_IMAGE_DIR;

@Component
public class ImageMapper {
    @Value("${spring.application.file-api.url}")
    private String fileApiUrl;

    public Set<String> getPathToAccommodationImage(Accommodation accommodation) {
        return accommodation.getImages().stream()
                .map(image -> fileApiUrl + "/" + FILE_MANAGER_ACCOMMODATION_IMAGE_DIR + "/retrieve/files/" + image.getImageUrl())
                .collect(Collectors.toSet());
    }
}
