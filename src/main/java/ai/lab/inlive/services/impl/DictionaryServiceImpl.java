package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.request.DictionaryCreateRequest;
import ai.lab.inlive.dto.request.DictionaryFilterRequest;
import ai.lab.inlive.dto.request.DictionaryUpdateRequest;
import ai.lab.inlive.dto.response.DictionaryListResponse;
import ai.lab.inlive.dto.response.DictionaryResponse;
import ai.lab.inlive.entities.Dictionary;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.repositories.DictionaryRepository;
import ai.lab.inlive.services.DictionaryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {

    private final DictionaryRepository dictionaryRepository;

    @Override
    @Transactional
    public DictionaryResponse createDictionary(DictionaryCreateRequest request) {
        log.info("Creating dictionary with key: {}", request.getKey());

        if (dictionaryRepository.existsByKeyAndIsDeletedFalse(request.getKey())) {
            throw new RuntimeException("Dictionary with key '" + request.getKey() + "' already exists");
        }

        Dictionary dictionary = new Dictionary();
        dictionary.setKey(dictionary.getKey());
        dictionary.setValue(request.getValue());

        Dictionary saved = dictionaryRepository.save(dictionary);
        log.info("Successfully created dictionary with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    public DictionaryResponse getDictionaryById(Long id) {
        log.info("Fetching dictionary by ID: {}", id);
        Dictionary dictionary = dictionaryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", "Dictionary not found with ID: " + id));
        return mapToResponse(dictionary);
    }

    @Override
    public List<DictionaryResponse> getAllDictionaries() {
        log.info("Fetching all dictionaries");
        List<Dictionary> dictionaries = dictionaryRepository.findAllByIsDeletedFalse();
        return dictionaries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DictionaryListResponse getDictionariesWithFilters(DictionaryFilterRequest filterRequest) {
        log.info("Fetching dictionaries with filters: {}", filterRequest);

        Sort sort = Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()), filterRequest.getSortBy());
        Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), sort);

        Page<Dictionary> dictionariesPage = dictionaryRepository.findWithFilters(
                filterRequest.getType(),
                filterRequest.getIsDeleted(),
                filterRequest.getKey(),
                filterRequest.getValue(),
                pageable
        );

        List<DictionaryResponse> dictionaries = dictionariesPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        DictionaryListResponse response = new DictionaryListResponse();
        response.setDictionaries(dictionaries);
        response.setTotalPages(dictionariesPage.getTotalPages());
        response.setTotalElements(dictionariesPage.getTotalElements());
        response.setCurrentPage(dictionariesPage.getNumber());
        response.setPageSize(dictionariesPage.getSize());
        return response;
    }

    @Override
    @Transactional
    public DictionaryResponse updateDictionary(Long id, DictionaryUpdateRequest request) {
        log.info("Updating dictionary with ID: {}", id);

        Dictionary dictionary = dictionaryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", "Dictionary not found with ID: " + id));

        if (request.getKey() != null) {
            if (dictionaryRepository.existsByKeyAndIsDeletedFalse(request.getKey())
                && (!dictionary.getKey().equals(request.getKey()))) {
                throw new RuntimeException("Dictionary with key '" + request.getKey() + "' already exists");
            }
        }

        if (request.getKey() != null) {
            dictionary.setKey(request.getKey());
        }

        if (request.getValue() != null) {
            dictionary.setValue(request.getValue());
        }

        Dictionary updated = dictionaryRepository.save(dictionary);
        log.info("Successfully updated dictionary with ID: {}", id);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDictionary(Long id) {
        log.info("Deleting dictionary with ID: {}", id);

        Dictionary dictionary = dictionaryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", "Dictionary not found with ID: " + id));

        // Use reflection to set the isDeleted field since there's no setter
        try {
            java.lang.reflect.Field isDeletedField = dictionary.getClass().getSuperclass().getDeclaredField("isDeleted");
            isDeletedField.setAccessible(true);
            isDeletedField.set(dictionary, true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("Error setting isDeleted field", e);
            throw new RuntimeException("Error deleting dictionary", e);
        }

        dictionaryRepository.save(dictionary);

        log.info("Successfully deleted dictionary with ID: {}", id);
    }

    private DictionaryResponse mapToResponse(Dictionary dictionary) {
        DictionaryResponse response = new DictionaryResponse();
        response.setId(dictionary.getId());
        response.setKey(String.valueOf(dictionary.getKey()));
        response.setValue(dictionary.getValue());
        response.setCreatedAt(dictionary.getCreatedAt());
        response.setUpdatedAt(dictionary.getUpdatedAt());
        return response;
    }
}
