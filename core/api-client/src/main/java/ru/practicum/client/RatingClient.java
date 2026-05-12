package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.rating.dto.RatingDto;

@FeignClient(name = "rating-service")
public interface RatingClient {

    @GetMapping("/ratings/{eventId}")
    RatingDto getEventRating(@PathVariable("eventId") Long eventId);
}