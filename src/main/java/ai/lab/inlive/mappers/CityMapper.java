package ai.lab.inlive.mappers;

import ai.lab.inlive.dto.response.CityResponse;
import ai.lab.inlive.entities.City;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CityMapper {
    CityResponse toDto(City city);
}
