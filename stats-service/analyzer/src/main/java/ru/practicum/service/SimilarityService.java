package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.EventSimilarity;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.stats.avro.EventSimilarityAvro;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarityService {

    private final EventSimilarityRepository repository;

    @Transactional
    public void saveOrUpdate(EventSimilarityAvro value) {
        log.debug("Processing event similarity: {} and {}, score={}",
                value.getEventA(), value.getEventB(), value.getScore()
        );

        EventSimilarity similarity = EventSimilarity.builder()
                .eventA(value.getEventA())
                .eventB(value.getEventB())
                .score(value.getScore())
                .created(value.getTimestamp())
                .build();

        repository.findByEventAAndEventB(similarity.getEventA(), similarity.getEventB())
                .ifPresent(oldEventSimilarity -> {
                    similarity.setId(oldEventSimilarity.getId());
                    log.debug("Existing similarity found, updating id={}",
                            oldEventSimilarity.getId()
                    );
                });

        repository.save(similarity);

        log.info("Saved similarity between events {} and {}, score={}",
                similarity.getEventA(), similarity.getEventB(), similarity.getScore()
        );
    }

    @Transactional(readOnly = true)
    public List<EventSimilarity> findContainsEventsScore(
            Set<Long> eventIds,
            int maxResults
    ) {
        Pageable pageable = PageRequest.of(0, maxResults);

        log.info("Searching top {} event similarity pairs by score (eventIds size={})",
                maxResults, eventIds.size()
        );

        List<EventSimilarity> result =
                repository.findTopByEventAInOrEventBInOrderByScoreDesc(
                        eventIds, eventIds, pageable
                );

        log.info("Found {} similarity pairs", result.size());

        return result;
    }

    @Transactional(readOnly = true)
    public List<EventSimilarity> findAllContainsEvent(long eventId) {

        log.info("Searching similarities for event {}", eventId);

        List<EventSimilarity> result =
                repository.findByEventAOrEventB(eventId);

        log.info("Found {} similarities for event {}", result.size(), eventId);

        return result;
    }
}