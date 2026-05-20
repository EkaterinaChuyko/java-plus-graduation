package ru.practicum.base.event;

import ru.practicum.model.EventSimilarity;
import ru.practicum.stats.avro.EventSimilarityAvro;

import java.util.List;

public interface EventSimilarityService {
    void save(EventSimilarityAvro eventSimilarityAvro);

    List<EventSimilarity> getSimilarEvents(long eventId);

    List<EventSimilarity> getSimilarEvents(List<Long> eventIds);
}