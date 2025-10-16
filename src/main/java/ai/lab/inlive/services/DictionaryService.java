package ai.lab.inlive.services;

import ai.lab.inlive.dto.params.DictionarySearchParams;
import ai.lab.inlive.dto.request.DictionaryCreateRequest;
import ai.lab.inlive.dto.request.DictionaryUpdateRequest;
import ai.lab.inlive.dto.response.DictionaryResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DictionaryService {

    void createDictionary(DictionaryCreateRequest request);

    DictionaryResponse getDictionaryById(Long id);

    Page<DictionaryResponse> searchWithParams(DictionarySearchParams dictionarySearchParams, Pageable pageable);

    @Transactional
    void updateDictionary(Long id, DictionaryUpdateRequest request);

    @Transactional
    void deleteDictionary(Long id);
}
