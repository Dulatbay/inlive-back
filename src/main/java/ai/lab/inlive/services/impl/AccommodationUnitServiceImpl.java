package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.request.AccUnitTariffCreateRequest;
import ai.lab.inlive.dto.request.AccommodationUnitCreateRequest;
import ai.lab.inlive.dto.response.AccUnitTariffResponse;
import ai.lab.inlive.entities.*;
import ai.lab.inlive.entities.enums.DictionaryKey;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.mappers.AccommodationUnitMapper;
import ai.lab.inlive.repositories.*;
import ai.lab.inlive.services.AccommodationUnitService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationUnitServiceImpl implements AccommodationUnitService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationUnitRepository accommodationUnitRepository;
    private final AccUnitTariffsRepository accUnitTariffsRepository;
    private final DictionaryRepository dictionaryRepository;
    private final AccommodationUnitMapper unitMapper;

    @Override
    @Transactional
    public void createUnit(AccommodationUnitCreateRequest request) {
        log.info("Creating accommodation unit for accommodation: {}", request.getAccommodationId());

        Accommodation accommodation = accommodationRepository.findByIdAndIsDeletedFalse(request.getAccommodationId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_NOT_FOUND", "Accommodation not found with ID: " + request.getAccommodationId()));

        AccommodationUnit unit = unitMapper.toEntity(request);
        unit.setAccommodation(accommodation);
        if (unit.getIsAvailable() == null) {
            unit.setIsAvailable(Boolean.TRUE);
        }

        List<AccUnitDictionary> unitDictionaries = new ArrayList<>();

        if (request.getServiceDictionaryIds() != null) {
            for (Long id : request.getServiceDictionaryIds()) {
                Dictionary d = dictionaryRepository.findByIdAndIsDeletedFalse(id)
                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", "Dictionary not found with ID: " + id));
                if (d.getKey() != DictionaryKey.ACC_SERVICE) {
                    throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY", "Dictionary ID " + id + " must have key ACC_SERVICE");
                }
                unitDictionaries.add(unitMapper.toDictionaryLink(accommodation, unit, d));
            }
        }

        if (request.getConditionDictionaryIds() != null) {
            for (Long id : request.getConditionDictionaryIds()) {
                Dictionary d = dictionaryRepository.findByIdAndIsDeletedFalse(id)
                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", "Dictionary not found with ID: " + id));
                if (d.getKey() != DictionaryKey.ACC_CONDITION) {
                    throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY", "Dictionary ID " + id + " must have key ACC_CONDITION");
                }
                unitDictionaries.add(unitMapper.toDictionaryLink(accommodation, unit, d));
            }
        }
        unit.setDictionaries(unitDictionaries);

        accommodationUnitRepository.save(unit);
        log.info("Successfully created accommodation unit with ID: {}", unit.getId());
    }

    @Override
    @Transactional
    public AccUnitTariffResponse addTariff(Long unitId, AccUnitTariffCreateRequest request) {
        log.info("Adding tariff for unit: {}", unitId);
        AccommodationUnit unit = accommodationUnitRepository.findByIdAndIsDeletedFalse(unitId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "ACCOMMODATION_UNIT_NOT_FOUND", "Accommodation Unit not found with ID: " + unitId));

        Dictionary rangeType = dictionaryRepository.findByIdAndIsDeletedFalse(request.getRangeTypeId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", "Dictionary not found with ID: " + request.getRangeTypeId()));

        if (rangeType.getKey() != DictionaryKey.RANGE_TYPE) {
            throw new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "INVALID_DICTIONARY_KEY", "Dictionary ID " + request.getRangeTypeId() + " must have key RANGE_TYPE");
        }

        AccUnitTariffs tariff = unitMapper.toEntity(request);
        tariff.setAccommodation(unit.getAccommodation());
        tariff.setUnit(unit);
        tariff.setRangeType(rangeType);

        AccUnitTariffs saved = accUnitTariffsRepository.save(tariff);

        AccUnitTariffResponse response = unitMapper.toDto(saved);
        log.info("Created tariff {} for unit {}", saved.getId(), unitId);
        return response;
    }
}
