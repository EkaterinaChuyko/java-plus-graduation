package ru.practicum.stats.client;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.stats.grpc.dashboard.RecommendationsControllerGrpc;
import ru.practicum.stats.grpc.recommendation.InteractionsCountRequestProto;
import ru.practicum.stats.grpc.recommendation.RecommendedEventProto;
import ru.practicum.stats.grpc.recommendation.SimilarEventsRequestProto;
import ru.practicum.stats.grpc.recommendation.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Component
public class AnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        log.debug("Get interactions count for events: {}", eventIds);

        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();

        log.debug("Request: {}", request);

        try {
            Iterator<RecommendedEventProto> iterator = client.getInteractionsCount(request);

            List<RecommendedEventProto> result = new ArrayList<>();
            iterator.forEachRemaining(result::add);

            log.debug("Successfully got interactions count: {} items", result.size());

            return result.stream();

        } catch (StatusRuntimeException exception) {
            log.error("gRPC getInteractionsCount failed", exception);
            throw new RuntimeException("gRPC getInteractionsCount failed", exception);
        }
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        log.debug("Get similar events for eventId={}, userId={}, maxResults={}",
                eventId, userId, maxResults);

        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        log.debug("Request: {}", request);

        try {
            Iterator<RecommendedEventProto> iterator = client.getSimilarEvents(request);

            List<RecommendedEventProto> result = new ArrayList<>();
            iterator.forEachRemaining(result::add);

            log.debug("Successfully got similar events: {} items", result.size());

            return result.stream();

        } catch (StatusRuntimeException exception) {
            log.error("gRPC getSimilarEvents failed", exception);
            throw new RuntimeException("gRPC getSimilarEvents failed", exception);
        }
    }

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        log.debug("Get recommendations for userId={}, maxResults={}", userId, maxResults);

        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        log.debug("Request: {}", request);

        try {
            Iterator<RecommendedEventProto> iterator = client.getRecommendationsForUser(request);

            List<RecommendedEventProto> result = new ArrayList<>();
            iterator.forEachRemaining(result::add);

            log.debug("Successfully got recommendations: {} items", result.size());

            return result.stream();

        } catch (StatusRuntimeException exception) {
            log.error("gRPC getRecommendationsForUser failed", exception);
            throw new RuntimeException("gRPC getRecommendationsForUser failed", exception);
        }
    }
}