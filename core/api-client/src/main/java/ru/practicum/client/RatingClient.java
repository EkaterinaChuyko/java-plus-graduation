package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.rating.RatingDto;

@FeignClient(name = "rating-service")
public interface RatingClient {

    @GetMapping("/internal/ratings/event/{eventId}")
    RatingDto getEventRating(@PathVariable("eventId") Long eventId);

}
