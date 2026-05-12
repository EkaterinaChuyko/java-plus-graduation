package ru.practicum.service;

import ru.practicum.rating.dto.RatingDto;
import ru.practicum.request.RateEventRequest;

public interface RatingService {
    void rateEvent(Long userId, RateEventRequest request);

    void deleteRating(Long userId, Long ratingId);

    RatingDto getEventRating(Long eventId);

    Boolean getUserRatingForEvent(Long userId, Long eventId);
}