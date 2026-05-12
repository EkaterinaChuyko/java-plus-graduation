package ru.practicum.service.event;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ConflictException;
import ru.practicum.client.*;
import ru.practicum.event.dto.event.EventFullDto;
import ru.practicum.event.dto.event.EventShortDto;
import ru.practicum.event.dto.event.EventState;
import ru.practicum.event.dto.event.NewEventDto;
import ru.practicum.mapper.EventMapper;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.request.*;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventServiceImpl implements EventService {

    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestClient requestClient;
    private final CategoryClient categoryClient;
    private final RatingClient ratingClient;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {

        UserDto user = userClient.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        if (dto.getEventDate() != null &&
            dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours in the future");
        }

        EventFullDto created = eventClient.createEvent(userId, dto);

        return enrichFullDtoFromDto(created);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {

        EventFullDto event = eventClient.getEvent(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event not found for this user");
        }

        return enrichFullDtoFromDto(event);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {

        UserDto user = userClient.getUserById(userId);
        if (user == null) throw new NotFoundException("User not found");

        return eventClient.getUserEvents(userId, from, size);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {

        EventFullDto event = eventClient.getEvent(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("User is not initiator");
        }

        return requestClient.getEventRequests(eventId);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest dto
    ) {

        EventFullDto event = eventClient.getEvent(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("User is not initiator");
        }

        long confirmedCount = requestClient.getConfirmedRequestsCount(eventId);

        if (event.getParticipantLimit() > 0 &&
            confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("The participant limit has been reached");
        }

        EventRequestStatusUpdateResult result =
                requestClient.updateRequestStatus(eventId, dto);

        return result;
    }

    @Override
    public List<EventShortDto> searchPublic(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            String sort,
            int from,
            int size,
            String requestUri,
            String ip
    ) {

        safeAddHit(requestUri, ip);

        return eventClient.searchPublic(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size
        );
    }

    @Override
    public EventFullDto getPublicById(Long eventId, String requestUri, String ip) {

        safeAddHit(requestUri, ip);

        EventFullDto event = eventClient.getEvent(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event must be published");
        }

        return enrichFullDtoFromDto(event);
    }

    @Override
    public List<EventFullDto> searchAdmin(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size
    ) {
        return eventClient.searchAdmin(
                        users,
                        states,
                        categories,
                        rangeStart,
                        rangeEnd,
                        from,
                        size
                )
                .stream()
                .map(this::enrichFullDtoFromDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest dto) {
        return eventClient.updateByAdmin(eventId, dto);
    }

    @Override
    @Transactional
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest dto) {
        return eventClient.updateByUser(userId, eventId, dto);
    }

    private void safeAddHit(String uri, String ip) {
        try {
            statsClient.hit(buildHit("ewm-main-service", uri, ip, LocalDateTime.now()));
        } catch (Exception e) {
            log.warn("Could not save stats hit: {}", e.getMessage());
        }
    }

    private long fetchViews(Long eventId) {
        try {
            List<ViewStatsDto> stats = statsClient.getStats(
                    LocalDateTime.now().minusYears(100),
                    LocalDateTime.now().plusSeconds(1),
                    List.of("/events/" + eventId),
                    true
            );
            return stats.isEmpty() ? 0 : stats.get(0).getHits();
        } catch (Exception e) {
            return 0;
        }
    }

    private HitDto buildHit(String app, String uri, String ip, LocalDateTime ts) {
        HitDto hit = new HitDto();
        hit.setApp(app);
        hit.setUri(uri);
        hit.setIp(ip);
        hit.setTimestamp(ts);
        return hit;
    }

    private EventFullDto enrichFullDtoFromDto(EventFullDto e) {

        long views = fetchViews(e.getId());
        long confirmed = requestClient.getConfirmedRequestsCount(e.getId());
        RatingDto rating = ratingClient.getEventRating(e.getId());

        return EventMapper.toFullFromDto(e, views, confirmed, rating);
    }

    private EventShortDto enrichShortDtoFromDto(EventFullDto e) {

        long views = fetchViews(e.getId());
        long confirmed = requestClient.getConfirmedRequestsCount(e.getId());
        RatingDto rating = ratingClient.getEventRating(e.getId());

        return EventMapper.toShortFromDto(e, views, confirmed, rating);
    }
}