package ai.lab.inlive.mappers;

import ai.lab.inlive.dto.response.UserResponse;
import ai.lab.inlive.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ImageMapper.class})
public interface UserMapper {
    @Mapping(target = "photoUrl", expression = "java(imageMapper.getPathToUserPhoto(user))")
    UserResponse toDto(User user, ImageMapper imageMapper);
}
