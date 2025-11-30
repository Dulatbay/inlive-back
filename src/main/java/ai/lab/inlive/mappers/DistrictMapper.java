package ai.lab.inlive.mappers;

import ai.lab.inlive.dto.response.DistrictResponse;
import ai.lab.inlive.entities.District;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DistrictMapper {
    DistrictResponse toDto(District district);

    @Mapping(source = "district.city.id", target = "cityId")
    @Mapping(source = "averagePrice", target = "averagePrice")
    DistrictResponse toDtoWithAvgPrice(District district, Double averagePrice);
}
