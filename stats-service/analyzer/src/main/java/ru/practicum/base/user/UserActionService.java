package ru.practicum.base.user;

import ru.practicum.model.UserAction;
import ru.practicum.stats.avro.UserActionAvro;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface UserActionService {
    void save(UserActionAvro userActionAvro);

    Map<Long, Double> getInteractionsCount(List<Long> eventIds);

    List<UserAction> getByUserId(long userId);

    Map<Long, Double> getUserScoresForEvents(long userId, Collection<Long> eventIds);
}