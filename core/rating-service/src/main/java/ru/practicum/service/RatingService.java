package ru.practicum.service;

import ru.practicum.dto.rating.RateEventRequest;
import ru.practicum.dto.rating.RatingDto;

public interface RatingService {
    void rateEvent(Long userId, RateEventRequest request);

    void deleteRating(Long userId, Long ratingId);

    RatingDto getEventRating(Long eventId);

    Boolean getUserRatingForEvent(Long userId, Long eventId);
}