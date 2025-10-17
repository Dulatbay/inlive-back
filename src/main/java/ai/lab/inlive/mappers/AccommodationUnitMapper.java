package ai.lab.inlive.mappers;

import ai.lab.inlive.dto.request.AccUnitTariffCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitCreateRequest;
import ai.lab.inlive.dto.response.AccUnitTariffResponse;
import ai.lab.inlive.entities.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

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

    @Named("normalizeCurrency")
    default String normalizeCurrency(String currency) {
        return (currency == null || currency.isBlank()) ? "KZT" : currency;
    }

    @Named("normalizeIsAvailable")
    default Boolean normalizeIsAvailable(Boolean isAvailable) {
        return isAvailable == null ? Boolean.TRUE : isAvailable;
    }
}
