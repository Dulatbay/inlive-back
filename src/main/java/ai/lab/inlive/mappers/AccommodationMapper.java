package ai.lab.inlive.mappers;

import ai.lab.inlive.dto.request.AccommodationCreateRequest;
import ai.lab.inlive.dto.response.AccommodationResponse;
import ai.lab.inlive.entities.AccImages;
import ai.lab.inlive.entities.Accommodation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ImageMapper.class})
public interface AccommodationMapper {
    @Mapping(target = "approvedBy", source = "accommodation.approvedBy.id")
    @Mapping(target = "ownerId", source = "accommodation.ownerId.id")
    @Mapping(target = "cityId", source = "accommodation.city.id")
    @Mapping(target = "cityName", source = "accommodation.city.name")
    @Mapping(target = "districtId", source = "accommodation.district.id")
    @Mapping(target = "districtName", source = "accommodation.district.name")
    @Mapping(target = "imageUrls", expression = "java(imageMapper.getPathToAccommodationImage(accommodation))")
    AccommodationResponse toDto(Accommodation accommodation, ImageMapper imageMapper);

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
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "dictionaries", ignore = true)
    @Mapping(target = "configs", ignore = true)
    @Mapping(target = "units", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Accommodation toEntity(AccommodationCreateRequest request);

    AccImages toImage(Accommodation accommodation, String imageUrl);
}
