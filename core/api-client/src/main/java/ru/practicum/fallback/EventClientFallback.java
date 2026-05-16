package ru.practicum.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.client.EventClient;
import ru.practicum.dto.event.EventShortDto;

@Component
@Slf4j
public class EventClientFallback implements EventClient {

    @Override
    public Boolean eventExists(Long eventId) {
        log.warn("Fallback: eventExists({})", eventId);
        return false;
    }

    @Override
    public EventShortDto getEventById(Long eventId) {
        log.warn("Fallback: getEventById({})", eventId);
        return null;
    }

    @Override
    public Boolean isEventPublished(Long eventId) {
        log.warn("Fallback: isEventPublished({})", eventId);
        return false;
    }
}
