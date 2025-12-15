package ai.lab.inlive.schedulers;

import ai.lab.inlive.entities.AccSearchRequest;
import ai.lab.inlive.entities.enums.SearchRequestStatus;
import ai.lab.inlive.repositories.AccSearchRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchRequestExpirationScheduler {
    private final AccSearchRequestRepository accSearchRequestRepository;

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void checkAndExpireSearchRequests() {
        log.debug("Starting search request expiration check...");

        List<AccSearchRequest> expiredRequests = accSearchRequestRepository.findExpiredRequests();

        if (!expiredRequests.isEmpty()) {
            log.info("Found {} expired search requests", expiredRequests.size());

            for (AccSearchRequest request : expiredRequests) {
                request.setStatus(SearchRequestStatus.EXPIRED);
                log.info("Search request ID {} expired. Was created at: {}, expired at: {}",
                        request.getId(),
                        request.getCreatedAt(),
                        request.getExpiresAt());
            }

            accSearchRequestRepository.saveAll(expiredRequests);
            log.info("Successfully expired {} search requests", expiredRequests.size());
        } else {
            log.debug("No expired search requests found");
        }
    }
}
