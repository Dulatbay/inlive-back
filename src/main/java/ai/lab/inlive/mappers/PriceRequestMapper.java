package ai.lab.inlive.mappers;

import ai.lab.inlive.dto.request.PriceRequestCreateRequest;
import ai.lab.inlive.dto.response.PriceRequestResponse;
import ai.lab.inlive.entities.PriceRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PriceRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "searchRequest", ignore = true)
    @Mapping(target = "unit", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "clientResponseStatus", ignore = true)
    PriceRequest toEntity(PriceRequestCreateRequest request);

    @Mapping(target = "searchRequestId", source = "searchRequest.id")
    @Mapping(target = "accommodationUnitId", source = "unit.id")
    @Mapping(target = "accommodationUnitName", source = "unit.name")
    @Mapping(target = "accommodationName", source = "unit.accommodation.name")
    @Mapping(target = "status", expression = "java(priceRequest.getStatus() != null ? priceRequest.getStatus().name() : null)")
    @Mapping(target = "clientResponseStatus", expression = "java(priceRequest.getClientResponseStatus() != null ? priceRequest.getClientResponseStatus().name() : null)")
    PriceRequestResponse toDto(PriceRequest priceRequest);
}

