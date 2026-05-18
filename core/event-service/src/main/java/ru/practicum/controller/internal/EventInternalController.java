package ru.practicum.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.service.event.EventService;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class EventInternalController {

    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventShortDto getEventById(@PathVariable Long eventId) {
        return eventService.getEventShortInternal(eventId);
    }

    @GetMapping("/{eventId}/exists")
    public Boolean eventExists(@PathVariable Long eventId) {
        return eventService.exists(eventId);
    }

    @GetMapping("/{eventId}/is-published")
    public Boolean isEventPublished(@PathVariable Long eventId) {
        return eventService.isPublished(eventId);
    }
}