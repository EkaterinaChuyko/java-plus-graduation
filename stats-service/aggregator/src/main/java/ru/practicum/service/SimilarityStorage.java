package ru.practicum.service;

import org.springframework.stereotype.Service;
import ru.practicum.model.EventPair;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimilarityStorage {

    private final Map<Long, Map<Long, Double>> userWeights =
            new ConcurrentHashMap<>();

    private final Map<Long, Double> eventWeightSums =
            new ConcurrentHashMap<>();

    private final Map<EventPair, Double> minWeightSums =
            new ConcurrentHashMap<>();

    public Map<Long, Double> getEventWeights(long eventId) {
        return userWeights.computeIfAbsent(
                eventId,
                id -> new ConcurrentHashMap<>()
        );
    }

    public double getEventWeightSum(long eventId) {
        return eventWeightSums.getOrDefault(eventId, 0.0);
    }

    public void setEventWeightSum(long eventId, double value) {
        eventWeightSums.put(eventId, value);
    }

    public double getMinWeightSum(long a, long b) {
        return minWeightSums.getOrDefault(new EventPair(a, b), 0.0);
    }

    public void setMinWeightSum(long a, long b, double value) {
        minWeightSums.put(new EventPair(a, b), value);
    }

    public Set<Long> getEvents() {
        return userWeights.keySet();
    }
}
