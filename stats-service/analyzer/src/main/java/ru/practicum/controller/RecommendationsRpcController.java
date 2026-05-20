package ru.practicum.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.base.event.EventSimilarityService;
import ru.practicum.base.user.UserActionService;
import ru.practicum.model.EventSimilarity;
import ru.practicum.model.UserAction;
import ru.practicum.stats.grpc.dashboard.RecommendationsControllerGrpc;
import ru.practicum.stats.grpc.recommendation.InteractionsCountRequestProto;
import ru.practicum.stats.grpc.recommendation.RecommendedEventProto;
import ru.practicum.stats.grpc.recommendation.SimilarEventsRequestProto;
import ru.practicum.stats.grpc.recommendation.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@GrpcService
public class RecommendationsRpcController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final UserActionService userActionService;
    private final EventSimilarityService eventSimilarityService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        log.debug("Received user predictions request: {}", request);

        try {
            List<UserAction> userActions = userActionService.getByUserId(request.getUserId());

            Set<Long> viewedEventIds = userActions.stream()
                    .map(UserAction::getEventId)
                    .collect(Collectors.toSet());

            if (viewedEventIds.isEmpty()) {
                log.debug("User has no history");
                responseObserver.onCompleted();
                return;
            }

            List<UserAction> lastNViewed = userActions.stream()
                    .sorted(Comparator.comparing(UserAction::getTimestamp).reversed())
                    .limit(request.getMaxResults())
                    .toList();

            List<Long> seedEvents = lastNViewed.stream()
                    .map(UserAction::getEventId)
                    .toList();

            List<Long> candidateEvents = eventSimilarityService
                    .getSimilarEvents(seedEvents).stream()
                    .filter(sim ->
                            !(viewedEventIds.contains(sim.getEventA())
                              && viewedEventIds.contains(sim.getEventB())))
                    .sorted(Comparator.comparingDouble(EventSimilarity::getScore).reversed())
                    .map(sim -> viewedEventIds.contains(sim.getEventA())
                            ? sim.getEventB()
                            : sim.getEventA())
                    .distinct()
                    .toList();

            List<Long> otherEvents = candidateEvents.stream()
                    .limit(request.getMaxResults())
                    .toList();

            log.debug("Candidate events: {}", otherEvents);

            for (Long otherEvent : otherEvents) {

                Map<Long, Double> similarities = eventSimilarityService
                        .getSimilarEvents(otherEvent).stream()
                        .filter(sim ->
                                viewedEventIds.contains(sim.getEventA())
                                || viewedEventIds.contains(sim.getEventB()))
                        .collect(Collectors.toMap(
                                sim -> viewedEventIds.contains(sim.getEventA())
                                        ? sim.getEventA()
                                        : sim.getEventB(),
                                EventSimilarity::getScore,
                                (a, b) -> a
                        ));

                log.debug("Similarities: {}", similarities);

                if (similarities.isEmpty()) {
                    continue;
                }

                Map<Long, Double> ratings =
                        userActionService.getUserScoresForEvents(request.getUserId(), similarities.keySet());

                log.debug("Ratings: {}", ratings);

                double numerator = similarities.entrySet().stream()
                        .mapToDouble(e ->
                                e.getValue() *
                                ratings.getOrDefault(e.getKey(), 0.0))
                        .sum();

                double denominator = similarities.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();

                if (denominator == 0.0) {
                    continue;
                }

                double predictedRating = numerator / denominator;

                responseObserver.onNext(
                        RecommendedEventProto.newBuilder()
                                .setEventId(otherEvent)
                                .setScore(predictedRating)
                                .build()
                );
            }

            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in getRecommendationsForUser", e);

            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        log.debug("Received similar events request: {}", request);

        try {
            Set<Long> viewedEventIds = userActionService.getByUserId(request.getUserId()).stream()
                    .map(UserAction::getEventId)
                    .collect(Collectors.toSet());

            List<RecommendedEventProto> result = eventSimilarityService
                    .getSimilarEvents(request.getEventId()).stream()
                    .filter(sim ->
                            !(viewedEventIds.contains(sim.getEventA())
                              && viewedEventIds.contains(sim.getEventB())))
                    .map(sim -> {
                        long similarEventId =
                                request.getEventId() == sim.getEventA()
                                        ? sim.getEventB()
                                        : sim.getEventA();

                        return new AbstractMap.SimpleEntry<>(similarEventId, sim.getScore());
                    })
                    .filter(e -> !viewedEventIds.contains(e.getKey()))
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(request.getMaxResults())
                    .map(e -> RecommendedEventProto.newBuilder()
                            .setEventId(e.getKey())
                            .setScore(e.getValue())
                            .build())
                    .toList();

            result.forEach(responseObserver::onNext);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in getSimilarEvents", e);

            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        log.debug("Received interactions count request: {}", request);

        try {
            Map<Long, Double> result =
                    userActionService.getInteractionsCount(request.getEventIdList());

            result.forEach((eventId, score) ->
                    responseObserver.onNext(
                            RecommendedEventProto.newBuilder()
                                    .setEventId(eventId)
                                    .setScore(score)
                                    .build()
                    )
            );

            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in getInteractionsCount", e);

            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }
}