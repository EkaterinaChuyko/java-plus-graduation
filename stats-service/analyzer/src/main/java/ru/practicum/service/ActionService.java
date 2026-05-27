package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.ActionType;
import ru.practicum.model.UserAction;
import ru.practicum.repository.UserActionRepository;
import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.avro.UserActionAvro;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionService {
    private final UserActionRepository repository;

    @Transactional
    public void saveOrUpdate(UserActionAvro avroAction) {

        long userId = avroAction.getUserId();
        long eventId = avroAction.getEventId();

        Optional<UserAction> oldActionOpt =
                repository.findByUserIdAndEventId(userId, eventId);

        ActionType action = mapAction(avroAction.getActionType());
        double rating = getRatingByActionType(avroAction.getActionType());

        if (oldActionOpt.isEmpty()) {

            Instant ts = avroAction.getTimestamp();

            UserAction actionEntity = UserAction.builder()
                    .userId(userId)
                    .eventId(eventId)
                    .action(action)
                    .rating(rating)
                    .timestamp(LocalDateTime.ofInstant(ts, ZoneOffset.UTC))
                    .created(ts)
                    .build();

            repository.save(actionEntity);

        } else {

            UserAction oldAction = oldActionOpt.get();

            if (rating >= oldAction.getRating()) {
                oldAction.setRating(rating);
                oldAction.setAction(action);

                Instant newTs = avroAction.getTimestamp();
                if (oldAction.getCreated() == null || oldAction.getCreated().isBefore(newTs)) {
                    oldAction.setCreated(newTs);
                }

                repository.save(oldAction);
            }
        }
    }

    private double getRatingByActionType(ActionTypeAvro actionType) {

        return switch (actionType) {
            case ACTION_VIEW -> 0.4;
            case ACTION_REGISTER -> 0.8;
            case ACTION_LIKE -> 1.0;
        };
    }

    @Transactional(readOnly = true)
    public Set<Long> findLatestEventsForUser(long userId, int maxResult) {
        Pageable pageable = PageRequest.of(0, maxResult);

        log.info("Fetching latest {} events for user {}", maxResult, userId);

        List<Long> eventIds =
                repository.findDistinctEventIdByUserIdOrderByCreatedDesc(userId, pageable);

        log.info("Found {} events for user {}", eventIds.size(), userId);

        return new HashSet<>(eventIds);
    }

    @Transactional(readOnly = true)
    public Set<Long> findActionsForUser(long userId, Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Set.of();
        }

        log.info("Fetching actions for user {}", userId);

        List<Long> result =
                repository.findDistinctEventIdByUserIdAndEventIdIn(userId, eventIds);

        log.info("Found {} events", result.size());

        return new HashSet<>(result);
    }

    @Transactional(readOnly = true)
    public List<UserAction> findActionsForEvents(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserAction> actions =
                repository.findAllByEventIdIn(eventIds).stream().toList();

        log.info("Found {} interactions", actions.size());

        return actions;
    }

    private ActionType mapAction(ActionTypeAvro avro) {
        return switch (avro) {
            case ACTION_VIEW -> ActionType.ACTION_VIEW;
            case ACTION_REGISTER -> ActionType.ACTION_REGISTER;
            case ACTION_LIKE -> ActionType.ACTION_LIKE;
        };
    }
}