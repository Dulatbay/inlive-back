package ai.lab.inlive.mappers;

import ai.lab.inlive.dto.request.AccommodationCreateRequest;
import ai.lab.inlive.dto.response.AccommodationResponse;
import ai.lab.inlive.dto.response.DictionaryResponse;
import ai.lab.inlive.entities.*;
import ai.lab.inlive.entities.enums.DictionaryKey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {ImageMapper.class})
public interface AccommodationMapper {
    @Mapping(target = "approvedBy", source = "accommodation.approvedBy.id")
    @Mapping(target = "ownerId", source = "accommodation.ownerId.id")
    @Mapping(target = "cityId", source = "accommodation.city.id")
    @Mapping(target = "cityName", source = "accommodation.city.name")
    @Mapping(target = "districtId", source = "accommodation.district.id")
    @Mapping(target = "districtName", source = "accommodation.district.name")
    @Mapping(target = "services", expression = "java(extractDictionariesByKey(accommodation, ai.lab.inlive.entities.enums.DictionaryKey.ACC_SERVICE))")
    @Mapping(target = "conditions", expression = "java(extractDictionariesByKey(accommodation, ai.lab.inlive.entities.enums.DictionaryKey.ACC_CONDITION))")
    @Mapping(target = "imageUrls", expression = "java(imageMapper.getPathToAccommodationImages(accommodation))")
    AccommodationResponse toDto(Accommodation accommodation, ImageMapper imageMapper);

    @Mapping(target = "key", expression = "java(dictionary.getKey().name())")
    DictionaryResponse dictionaryToDto(Dictionary dictionary);

    default Set<DictionaryResponse> extractDictionariesByKey(Accommodation accommodation, DictionaryKey key) {
        if (accommodation.getDictionaries() == null) {
            return Set.of();
        }
        return accommodation.getDictionaries().stream()
                .filter(ud -> ud.getDictionary() != null && ud.getDictionary().getKey() == key)
                .map(ud -> dictionaryToDto(ud.getDictionary()))
                .collect(Collectors.toSet());
    }

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

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accommodation", source = "accommodation")
    @Mapping(target = "dictionary", source = "dictionary")
    AccDictionary toDictionaryLink(Accommodation accommodation, Dictionary dictionary);
}
