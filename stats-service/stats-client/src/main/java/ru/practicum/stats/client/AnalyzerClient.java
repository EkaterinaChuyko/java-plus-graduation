package ru.practicum.stats.client;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import ru.practicum.stats.grpc.dashboard.RecommendationsControllerGrpc;
import ru.practicum.stats.grpc.recommendation.InteractionsCountRequestProto;
import ru.practicum.stats.grpc.recommendation.UserPredictionsRequestProto;

import java.util.*;

@Slf4j
@Component
public class AnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub recommendationsStub;

    @Retryable(
            retryFor = {StatusRuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000)
    )
    public Map<Long, Double> getEventRatings(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        try {
            InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                    .addAllEventId(eventIds)
                    .build();

            Map<Long, Double> ratings = new HashMap<>();
            recommendationsStub.getInteractionsCount(request).forEachRemaining(response -> {
                ratings.put(response.getEventId(), response.getScore());
            });

            log.info("Received ratings for {} events", ratings.size());
            return ratings;

        } catch (StatusRuntimeException e) {
            log.error("Failed to get event ratings. Status: {}, message: {}",
                    e.getStatus(), e.getMessage(), e);
            throw e;

        } catch (Exception e) {
            log.error("Failed to get event ratings. Exception: {}, message: {}",
                    e.getClass().getName(), e.getMessage(), e);
            throw new RuntimeException("Error while fetching event ratings", e);
        }
    }

    @Retryable(
            retryFor = {StatusRuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000)
    )
    public Map<Long, Double> getRecommendationsForUser(Long userId, Integer maxResults) {
        if (userId == null) {
            return Map.of();
        }

        try {
            UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults != null ? maxResults : 10)
                    .build();

            Map<Long, Double> recommendations = new HashMap<>();
            recommendationsStub.getRecommendationsForUser(request).forEachRemaining(response -> {
                recommendations.put(response.getEventId(), response.getScore());
            });

            log.info("Received recommendations for user {}: {} events",
                    userId, recommendations.size());

            return recommendations;

        } catch (StatusRuntimeException e) {
            log.error("Failed to get recommendations for user {}. Status: {}, message: {}",
                    userId, e.getStatus(), e.getMessage(), e);
            throw e;

        } catch (Exception e) {
            log.error("Failed to get recommendations for user {}. Exception: {}, message: {}",
                    userId, e.getClass().getName(), e.getMessage(), e);
            throw new RuntimeException("Error while fetching user recommendations", e);
        }
    }
}