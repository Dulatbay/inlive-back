package ai.lab.inlive.mappers;

import ai.lab.inlive.dto.response.DistrictResponse;
import ai.lab.inlive.entities.District;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DistrictMapper {
    DistrictResponse toDto(District district);
}
