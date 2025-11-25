package ai.lab.inlive.mappers;

import ai.lab.inlive.dto.response.UserResponse;
import ai.lab.inlive.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toDto(User user);
}
