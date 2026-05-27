package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.model.EventSimilarity;
import ru.practicum.model.UserAction;
import ru.practicum.stats.grpc.recommendation.InteractionsCountRequestProto;
import ru.practicum.stats.grpc.recommendation.RecommendedEventProto;
import ru.practicum.stats.grpc.recommendation.SimilarEventsRequestProto;
import ru.practicum.stats.grpc.recommendation.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyzerService {

    private final ActionService actionService;
    private final SimilarityService similarityService;

    public Iterable<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        log.info("Request for user recommendations: userId={}, maxResults={}",
                request.getUserId(), request.getMaxResults()
        );

        int maxResults = Math.toIntExact(request.getMaxResults());

        Set<Long> actionIds =
                actionService.findLatestEventsForUser(request.getUserId(), maxResults);

        log.debug("Latest user events userId={}: {}", request.getUserId(), actionIds);

        if (actionIds.isEmpty()) {
            return List.of();
        }

        List<EventSimilarity> similarities =
                similarityService.findContainsEventsScore(actionIds, maxResults);

        log.debug("Found {} similarity records for userId={}",
                similarities.size(), request.getUserId());

        Map<Long, Double> eventIds = similarities.stream()
                .collect(Collectors.toMap(
                        o -> actionIds.contains(o.getEventA()) ? o.getEventB() : o.getEventA(),
                        EventSimilarity::getScore,
                        Double::max
                ));

        log.info("Generated {} recommendations for userId={}",
                eventIds.size(), request.getUserId());

        return eventIds.entrySet().stream()
                .map(o -> RecommendedEventProto.newBuilder()
                        .setEventId(o.getKey())
                        .setScore(o.getValue())
                        .build())
                .toList();
    }

    public Iterable<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        log.info("Request for similar events: eventId={}, userId={}, maxResults={}",
                request.getEventId(), request.getUserId(), request.getMaxResults()
        );

        List<EventSimilarity> similarPair =
                similarityService.findAllContainsEvent(request.getEventId());

        log.debug("Found {} similarity pairs for eventId={}",
                similarPair.size(), request.getEventId());

        Set<Long> ids = similarPair.stream()
                .map(EventSimilarity::getEventA)
                .collect(Collectors.toSet());

        Set<Long> otherIds = similarPair.stream()
                .map(EventSimilarity::getEventB)
                .collect(Collectors.toSet());

        ids.addAll(otherIds);

        Set<Long> userEventIds =
                actionService.findActionsForUser(request.getUserId(), ids);

        log.debug("User userId={} already interacted with events: {}",
                request.getUserId(), userEventIds);

        int beforeFilter = similarPair.size();

        similarPair.removeIf(o ->
                userEventIds.contains(o.getEventA())
                && userEventIds.contains(o.getEventB())
        );

        log.debug("Filtered {} similarity pairs (already seen by user)",
                beforeFilter - similarPair.size());

        return similarPair.stream()
                .sorted(Comparator.comparing(
                        EventSimilarity::getScore,
                        Comparator.reverseOrder()
                ))
                .limit(request.getMaxResults())
                .map(o -> {
                    long eventId = Objects.equals(o.getEventA(), request.getEventId())
                            ? o.getEventB()
                            : o.getEventA();

                    return RecommendedEventProto.newBuilder()
                            .setEventId(eventId)
                            .setScore(o.getScore())
                            .build();
                })
                .toList();
    }

    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        log.info("Request for interactions count for {} events",
                request.getEventIdList().size());

        Set<Long> eventIds = new HashSet<>(request.getEventIdList());

        List<UserAction> userActions =
                actionService.findActionsForEvents(eventIds);

        log.debug("Found {} user actions", userActions.size());

        Map<Long, Double> actionMap = userActions.stream()
                .collect(Collectors.groupingBy(
                        UserAction::getEventId,
                        Collectors.summingDouble(UserAction::getRating)
                ));

        log.info("Computed interactions for {} events", actionMap.size());

        return actionMap.entrySet().stream()
                .map(o -> RecommendedEventProto.newBuilder()
                        .setEventId(o.getKey())
                        .setScore(o.getValue())
                        .build())
                .toList();
    }
}

