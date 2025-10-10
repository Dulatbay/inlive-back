package ai.lab.inlive.services;

import ai.lab.inlive.dto.request.DictionaryCreateRequest;
import ai.lab.inlive.dto.request.DictionaryFilterRequest;
import ai.lab.inlive.dto.request.DictionaryUpdateRequest;
import ai.lab.inlive.dto.response.DictionaryListResponse;
import ai.lab.inlive.dto.response.DictionaryResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface DictionaryService {

    DictionaryResponse createDictionary(DictionaryCreateRequest request);

    DictionaryResponse getDictionaryById(Long id);

    List<DictionaryResponse> getAllDictionaries();

    List<DictionaryResponse> getDictionariesByType(String type);

    DictionaryListResponse getDictionariesWithFilters(DictionaryFilterRequest filterRequest);

    @Transactional
    DictionaryResponse updateDictionary(Long id, DictionaryUpdateRequest request);

    @Transactional
    void deleteDictionary(Long id);
}
