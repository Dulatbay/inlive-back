package ai.lab.inlive.mappers;

import ai.lab.inlive.dto.response.DictionaryResponse;
import ai.lab.inlive.entities.Dictionary;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DictionaryMapper {
    DictionaryResponse toDto(Dictionary dictionary);
}
