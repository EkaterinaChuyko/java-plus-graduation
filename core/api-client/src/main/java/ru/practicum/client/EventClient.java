package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.fallback.EventClientFallback;

@FeignClient(name = "event-service", fallback = EventClientFallback.class)
public interface EventClient {

    @GetMapping("/internal/events/{eventId}/exists")
    Boolean eventExists(@PathVariable("eventId") Long eventId);

    @GetMapping("/internal/events/{eventId}")
    EventShortDto getEventById(@PathVariable("eventId") Long eventId);

    @GetMapping("/internal/events/{eventId}/is-published")
    Boolean isEventPublished(@PathVariable("eventId") Long eventId);

}