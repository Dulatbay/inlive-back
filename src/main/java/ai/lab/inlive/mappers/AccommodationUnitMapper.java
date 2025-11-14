package ai.lab.inlive.mappers;

import ai.lab.inlive.dto.request.AccUnitTariffCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitCreateRequest;
import ai.lab.inlive.dto.response.AccUnitTariffResponse;
import ai.lab.inlive.dto.response.AccommodationUnitResponse;
import ai.lab.inlive.dto.response.DictionaryResponse;
import ai.lab.inlive.entities.*;
import ai.lab.inlive.entities.enums.DictionaryKey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AccommodationUnitMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accommodation", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "dictionaries", ignore = true)
    @Mapping(target = "tariffs", ignore = true)
    AccommodationUnit toEntity(AccommodationUnitCreateRequest request);

    @Mapping(target = "rangeTypeId", source = "rangeType.id")
    @Mapping(target = "rangeTypeKey", expression = "java(tariff.getRangeType().getKey().name())")
    @Mapping(target = "rangeTypeValue", source = "rangeType.value")
    AccUnitTariffResponse toDto(AccUnitTariffs tariff);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accommodation", ignore = true)
    @Mapping(target = "unit", ignore = true)
    @Mapping(target = "rangeType", ignore = true)
    @Mapping(target = "currency", source = "currency", qualifiedByName = "normalizeCurrency")
    AccUnitTariffs toEntity(AccUnitTariffCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accommodation", source = "accommodation")
    @Mapping(target = "unit", source = "unit")
    @Mapping(target = "dictionary", source = "dictionary")
    AccUnitDictionary toDictionaryLink(Accommodation accommodation, AccommodationUnit unit, Dictionary dictionary);

    @Mapping(target = "accommodationId", source = "accommodation.id")
    @Mapping(target = "unitType", expression = "java(unit.getUnitType() != null ? unit.getUnitType().name() : null)")
    @Mapping(target = "services", expression = "java(extractDictionariesByKey(unit, ai.lab.inlive.entities.enums.DictionaryKey.ACC_SERVICE))")
    @Mapping(target = "conditions", expression = "java(extractDictionariesByKey(unit, ai.lab.inlive.entities.enums.DictionaryKey.ACC_CONDITION))")
    @Mapping(target = "tariffs", expression = "java(mapTariffs(unit))")
    AccommodationUnitResponse toDto(AccommodationUnit unit);

    @Mapping(target = "key", expression = "java(dictionary.getKey().name())")
    DictionaryResponse dictionaryToDto(Dictionary dictionary);

    @Named("normalizeCurrency")
    default String normalizeCurrency(String currency) {
        return (currency == null || currency.isBlank()) ? "KZT" : currency;
    }

    @Named("normalizeIsAvailable")
    default Boolean normalizeIsAvailable(Boolean isAvailable) {
        return isAvailable == null ? Boolean.TRUE : isAvailable;
    }

    default Set<DictionaryResponse> extractDictionariesByKey(AccommodationUnit unit, DictionaryKey key) {
        if (unit.getDictionaries() == null) {
            return Set.of();
        }
        return unit.getDictionaries().stream()
                .filter(ud -> ud.getDictionary() != null && ud.getDictionary().getKey() == key)
                .map(ud -> dictionaryToDto(ud.getDictionary()))
                .collect(Collectors.toSet());
    }

    default Set<AccUnitTariffResponse> mapTariffs(AccommodationUnit unit) {
        if (unit.getTariffs() == null) {
            return Set.of();
        }
        return unit.getTariffs().stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }

    AccUnitImages toImage(Accommodation accommodation, AccommodationUnit unit, String imageUrl);
}
