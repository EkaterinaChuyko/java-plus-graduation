package ru.practicum.controller.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.rating.RatingDto;
import ru.practicum.service.RatingService;

@RestController
@RequestMapping("/internal/ratings")
@RequiredArgsConstructor
@Slf4j
public class RatingInternalController {

    private final RatingService ratingService;

    @GetMapping("/events/{eventId}")
    public RatingDto getEventRating(@PathVariable Long eventId) {
        log.debug("Internal API: get rating for event {}", eventId);
        return ratingService.getEventRating(eventId);
    }
}
