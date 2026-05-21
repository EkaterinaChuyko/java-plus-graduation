package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.UserAction;
import ru.practicum.repository.UserActionRepository;
import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.avro.UserActionAvro;

import java.time.Instant;
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

        log.debug("Processing interaction: userId={}, eventId={}, actionType={}",
                userId, eventId, avroAction.getActionType()
        );

        Optional<UserAction> oldActionOpt =
                repository.findByUserIdAndEventId(userId, eventId);

        if (oldActionOpt.isEmpty()) {
            double rating = getRatingByActionType(avroAction.getActionType());

            UserAction action = UserAction.builder()
                    .userId(userId)
                    .eventId(eventId)
                    .rating(rating)
                    .created(avroAction.getTimestamp())
                    .build();

            repository.save(action);

            log.info("Saved new interaction: userId={}, eventId={}, rating={}",
                    userId, eventId, rating
            );
        } else {
            UserAction oldAction = oldActionOpt.get();
            double oldRating = oldAction.getRating();
            double newRating = getRatingByActionType(avroAction.getActionType());

            if (newRating >= oldRating) {
                oldAction.setRating(newRating);

                Instant oldTimestamp = oldAction.getCreated();
                Instant newTimestamp = avroAction.getTimestamp();

                if (oldTimestamp == null || oldTimestamp.isBefore(newTimestamp)) {
                    oldAction.setCreated(newTimestamp);
                }

                repository.save(oldAction);

                log.info("Updated interaction: userId={}, eventId={}, oldRating={}, newRating={}",
                        userId, eventId, oldRating, newRating
                );
            } else {
                log.info("Update skipped: new rating is lower or equal to existing rating");
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
}