package ai.lab.inlive.services.impl;

import ai.lab.inlive.dto.params.DictionarySearchParams;
import ai.lab.inlive.dto.request.DictionaryCreateRequest;
import ai.lab.inlive.dto.request.DictionaryUpdateRequest;
import ai.lab.inlive.dto.response.DictionaryResponse;
import ai.lab.inlive.entities.Dictionary;
import ai.lab.inlive.exceptions.DbObjectNotFoundException;
import ai.lab.inlive.mappers.DictionaryMapper;
import ai.lab.inlive.repositories.DictionaryRepository;
import ai.lab.inlive.services.DictionaryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {

    private final DictionaryRepository dictionaryRepository;
    private final DictionaryMapper mapper;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public void createDictionary(DictionaryCreateRequest request) {
        log.info("Creating dictionary with key: {}", request.getKey());

        Dictionary dictionary = new Dictionary();
        dictionary.setKey(request.getKey());
        dictionary.setValue(request.getValue());

        Dictionary saved = dictionaryRepository.save(dictionary);
        log.info("Successfully created dictionary with ID: {}", saved.getId());
    }

    @Override
    public DictionaryResponse getDictionaryById(Long id) {
        log.info("Fetching dictionary by ID: {}", id);
        Dictionary dictionary = dictionaryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", 
                        messageSource.getMessage("services.dictionary.notFound", new Object[]{id}, LocaleContextHolder.getLocale())));
        return mapper.toDto(dictionary);
    }

    @Override
    public Page<DictionaryResponse> searchWithParams(DictionarySearchParams dictionarySearchParams, Pageable pageable) {
        log.info("Searching dictionaries with params: {}", dictionarySearchParams);

        var dictionaries = dictionaryRepository.findWithFilters(dictionarySearchParams, pageable);

        return dictionaries.map(mapper::toDto);
    }

    @Override
    @Transactional
    public void updateDictionary(Long id, DictionaryUpdateRequest request) {
        log.info("Updating dictionary with ID: {}", id);

        Dictionary dictionary = dictionaryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", 
                        messageSource.getMessage("services.dictionary.notFound", new Object[]{id}, LocaleContextHolder.getLocale())));

        if (request.getKey() != null) {
            if (dictionaryRepository.existsByKeyAndIsDeletedFalse(request.getKey())
                    && (!dictionary.getKey().equals(request.getKey()))) {
                throw new RuntimeException(messageSource.getMessage("services.dictionary.alreadyExists", 
                        new Object[]{request.getKey()}, LocaleContextHolder.getLocale()));
            }
        }

        if (request.getKey() != null) {
            dictionary.setKey(request.getKey());
        }

        if (request.getValue() != null) {
            dictionary.setValue(request.getValue());
        }

        dictionaryRepository.save(dictionary);
        log.info("Successfully updated dictionary with ID: {}", id);
    }

    @Override
    @Transactional
    public void deleteDictionary(Long id) {
        log.info("Deleting dictionary with ID: {}", id);

        Dictionary dictionary = dictionaryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "DICTIONARY_NOT_FOUND", 
                        messageSource.getMessage("services.dictionary.notFound", new Object[]{id}, LocaleContextHolder.getLocale())));

        dictionary.softDelete();

        dictionaryRepository.save(dictionary);

        log.info("Successfully deleted dictionary with ID: {}", id);
    }
}
