package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.config.ActionWeightsConfig;
import ru.practicum.producer.SimilarityProducer;
import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.avro.UserActionAvro;


import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AggregationService {

    private final SimilarityStorage storage;
    private final SimilarityProducer producer;
    private final SimilarityCalculator calculator;
    private final ActionWeightsConfig weightsConfig;

    public void process(UserActionAvro action) {

        double weight = getWeight(action);

        long userId = action.getUserId();
        long eventId = action.getEventId();

        Map<Long, Double> eventWeights =
                storage.getEventWeights(eventId);

        double oldWeight =
                eventWeights.getOrDefault(userId, 0.0);

        if (weight <= oldWeight) {
            return;
        }

        eventWeights.put(userId, weight);

        double delta = weight - oldWeight;

        double newEventSum =
                storage.getEventWeightSum(eventId) + delta;

        storage.setEventWeightSum(eventId, newEventSum);

        recalculate(eventId, userId, oldWeight, weight);
    }

    private EventSimilarityAvro buildSimilarity(
            long eventA,
            long eventB,
            double similarity
    ) {

        return EventSimilarityAvro.newBuilder()
                .setEventA(Math.min(eventA, eventB))
                .setEventB(Math.max(eventA, eventB))
                .setScore(similarity)
                .setTimestamp(Instant.now())
                .build();
    }

    private void recalculate(
            long eventId,
            long userId,
            double oldWeight,
            double newWeight
    ) {

        for (Long otherEvent : storage.getEvents()) {

            if (eventId == otherEvent) {
                continue;
            }

            Map<Long, Double> otherWeights =
                    storage.getEventWeights(otherEvent);

            Double otherUserWeight =
                    otherWeights.get(userId);

            if (otherUserWeight == null) {
                continue;
            }

            double oldMin =
                    Math.min(oldWeight, otherUserWeight);

            double newMin =
                    Math.min(newWeight, otherUserWeight);

            double delta = newMin - oldMin;

            double minSum =
                    storage.getMinWeightSum(eventId, otherEvent)
                    + delta;

            storage.setMinWeightSum(eventId, otherEvent, minSum);

            double similarity =
                    calculator.calculate(
                            minSum,
                            storage.getEventWeightSum(eventId),
                            storage.getEventWeightSum(otherEvent)
                    );

            if (similarity > 0) {
                producer.send(buildSimilarity(
                        eventId,
                        otherEvent,
                        similarity
                ));
            }
        }
    }

    private double getWeight(UserActionAvro action) {
        return switch (action.getActionType()) {
            case ACTION_VIEW -> weightsConfig.getView();
            case ACTION_REGISTER -> weightsConfig.getRegister();
            case ACTION_LIKE -> weightsConfig.getLike();
        };
    }
}
