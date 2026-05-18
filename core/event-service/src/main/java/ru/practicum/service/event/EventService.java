package ru.practicum.service.event;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.request.EventRequestStatusUpdateRequest;
import ru.practicum.request.EventRequestStatusUpdateResult;
import ru.practicum.request.UpdateEventAdminRequest;
import ru.practicum.request.UpdateEventUserRequest;

import java.time.LocalDateTime;
import java.util.*;

public interface EventService {

    EventFullDto create(Long userId, NewEventDto dto);

    EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest dto);

    EventFullDto getUserEvent(Long userId, Long eventId);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest dto);

    List<EventShortDto> searchPublic(String text, List<Long> categories, Boolean paid,
                                     LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                     Boolean onlyAvailable, String sort, int from, int size, String requestUri, String ip);

    EventFullDto getPublicById(Long eventId, String requestUri, String ip);

    List<EventFullDto> searchAdmin(List<Long> users, List<String> states, List<Long> categories,
                                   LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest dto);

    EventShortDto getEventShortInternal(Long eventId);

    Boolean exists(Long eventId);

    Boolean isPublished(Long eventId);
}