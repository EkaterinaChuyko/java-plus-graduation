package ru.practicum.controller.publicapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.service.RatingService;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicRatingController {

    private final RatingService ratingService;

    @GetMapping("/events/{eventId}/rating")
    public RatingDto getEventRating(@PathVariable @Min(1) Long eventId) {
        log.info("Getting rating for event {}", eventId);
        return ratingService.getEventRating(eventId);
    }
}