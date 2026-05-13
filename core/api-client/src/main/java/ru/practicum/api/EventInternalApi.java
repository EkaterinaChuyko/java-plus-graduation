package ru.practicum.api;

import ru.practicum.dto.event.EventShortDto;

public interface EventInternalApi {

    Boolean eventExists(Long eventId);

    EventShortDto getEventById(Long eventId);

    Boolean isEventPublished(Long eventId);
}
