package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ConflictException;
import ru.practicum.NotFoundException;
import ru.practicum.client.EventClient;
import ru.practicum.client.RequestClient;
import ru.practicum.client.UserClient;
import ru.practicum.event.dto.event.EventFullDto;
import ru.practicum.event.dto.event.EventState;
import ru.practicum.mapper.RatingMapper;
import ru.practicum.rating.dto.EventRating;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.repository.RatingRepository;
import ru.practicum.request.RateEventRequest;
import ru.practicum.user.dto.UserDto;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestClient requestClient;

    @Override
    @Transactional
    public void rateEvent(Long userId, RateEventRequest request) {

        log.debug("User {} rating event {} as {}", userId, request.getEventId(), request.getIsLike());

        UserDto user = userClient.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        EventFullDto event = eventClient.getEvent(request.getEventId());
        if (event == null) {
            throw new NotFoundException("Event not found");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Cannot rate own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot rate unpublished event");
        }

        Boolean participated = requestClient.hasConfirmedRequest(
                userId,
                request.getEventId()
        );

        if (!Boolean.TRUE.equals(participated)) {
            throw new ConflictException("Must have participated in event to rate it");
        }

        EventRating existingRating = ratingRepository
                .findByUserIdAndEventId(userId, request.getEventId())
                .orElse(null);

        if (existingRating != null) {
            existingRating.setIsLike(request.getIsLike());
            existingRating.setCreated(LocalDateTime.now());
            ratingRepository.save(existingRating);

        } else {
            EventRating rating = EventRating.builder()
                    .userId(userId)
                    .eventId(request.getEventId())
                    .isLike(request.getIsLike())
                    .created(LocalDateTime.now())
                    .build();

            ratingRepository.save(rating);
        }
    }

    @Override
    public RatingDto getEventRating(Long eventId) {

        EventFullDto event = eventClient.getEvent(eventId);
        if (event == null) {
            throw new NotFoundException("Event not found");
        }

        long likes = ratingRepository.countByEventIdAndIsLikeTrue(eventId);
        long dislikes = ratingRepository.countByEventIdAndIsLikeFalse(eventId);
        long total = likes + dislikes;

        if (total == 0) {
            return RatingDto.builder()
                    .score(null)
                    .likes(0L)
                    .dislikes(0L)
                    .total(0L)
                    .build();
        }

        return RatingMapper.toRatingDto(eventId, likes, dislikes);
    }

    @Override
    @Transactional
    public void deleteRating(Long userId, Long eventId) {

        EventRating rating = ratingRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Rating not found"));

        if (!rating.getUserId().equals(userId)) {
            throw new ConflictException("Cannot delete other user's rating");
        }

        ratingRepository.delete(rating);
    }

    @Override
    public Boolean getUserRatingForEvent(Long userId, Long eventId) {
        log.debug("Getting user {} rating for event {}", userId, eventId);

        return ratingRepository.findByUserIdAndEventId(userId, eventId)
                .map(EventRating::getIsLike)
                .orElse(null);
    }
}