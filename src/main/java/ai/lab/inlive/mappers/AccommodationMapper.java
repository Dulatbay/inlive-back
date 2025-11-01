package ai.lab.inlive.mappers;

import ai.lab.inlive.dto.request.AccommodationCreateRequest;
import ai.lab.inlive.dto.response.AccommodationResponse;
import ai.lab.inlive.entities.Accommodation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccommodationMapper {
    @Mapping(target = "approvedBy", source = "approvedBy.id")
    @Mapping(target = "ownerId", source = "ownerId.id")
    @Mapping(target = "cityId", source = "city.id")
    @Mapping(target = "cityName", source = "city.name")
    @Mapping(target = "districtId", source = "district.id")
    @Mapping(target = "districtName", source = "district.name")
    AccommodationResponse toDto(Accommodation accommodation);

    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "district", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "documents", ignore = true)
    Accommodation toEntity(AccommodationResponse dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "district", ignore = true)
    @Mapping(target = "approved", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "rating", constant = "0.0")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "dictionaries", ignore = true)
    @Mapping(target = "configs", ignore = true)
    @Mapping(target = "units", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Accommodation toEntity(AccommodationCreateRequest request);
    
    List<AccommodationResponse> toDto(List<Accommodation> accommodations);
    List<Accommodation> toEntity(List<AccommodationResponse> dtos);
}
