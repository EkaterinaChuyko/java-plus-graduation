package ru.practicum.mapper;

import ru.practicum.event.dto.category.CategoryDto;
import ru.practicum.event.dto.event.*;
import ru.practicum.model.Event;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.request.UpdateEventAdminRequest;
import ru.practicum.request.UpdateEventUserRequest;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event toEntity(NewEventDto dto, Long initiatorId) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .title(dto.getTitle())
                .categoryId(dto.getCategory())
                .eventDate(dto.getEventDate())
                .locationLat(dto.getLocation().getLat())
                .locationLon(dto.getLocation().getLon())
                .paid(dto.getPaid() != null ? dto.getPaid() : false)
                .participantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0)
                .requestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true)
                .initiatorId(initiatorId)
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .build();
    }

    public static EventShortDto toShort(Event e, CategoryDto catDto, UserShortDto userDto,
                                        long views, long confirmedRequests, RatingDto rating) {
        return new EventShortDto(
                e.getId(),
                e.getAnnotation(),
                catDto,
                confirmedRequests,
                e.getEventDate(),
                userDto,
                e.getPaid(),
                e.getTitle(),
                views,
                rating
        );
    }

    public static EventShortDto toShort(Event e, CategoryDto catDto, UserShortDto userDto,
                                        long views, long confirmedRequests) {
        return toShort(e, catDto, userDto, views, confirmedRequests, null);
    }

    public static EventShortDto toShort(Event e, long views) {
        return new EventShortDto(
                e.getId(),
                e.getAnnotation(),
                null,
                0L,
                e.getEventDate(),
                null,
                e.getPaid(),
                e.getTitle(),
                views,
                null
        );
    }

    public static EventFullDto toFull(Event e, CategoryDto catDto, UserShortDto userDto,
                                      long views, long confirmedRequests, RatingDto rating) {

        LocationDto loc = new LocationDto(e.getLocationLat(), e.getLocationLon());

        return EventFullDto.builder()
                .id(e.getId())
                .annotation(e.getAnnotation())
                .description(e.getDescription())
                .category(catDto)
                .confirmedRequests(confirmedRequests)
                .eventDate(e.getEventDate())
                .initiator(userDto)
                .location(loc)
                .paid(e.getPaid())
                .participantLimit(e.getParticipantLimit())
                .requestModeration(e.getRequestModeration())
                .state(e.getState())
                .createdOn(e.getCreatedOn())
                .publishedOn(e.getPublishedOn())
                .title(e.getTitle())
                .views(views)
                .rating(rating)
                .build();
    }

    public static void applyUserUpdate(Event e, UpdateEventUserRequest dto) {
        if (dto.getAnnotation() != null) e.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getTitle() != null) e.setTitle(dto.getTitle());
        if (dto.getCategory() != null) e.setCategoryId(dto.getCategory());
        if (dto.getEventDate() != null) e.setEventDate(dto.getEventDate());
        if (dto.getLocation() != null) {
            e.setLocationLat(dto.getLocation().getLat());
            e.setLocationLon(dto.getLocation().getLon());
        }
        if (dto.getPaid() != null) e.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) e.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) e.setRequestModeration(dto.getRequestModeration());
    }

    public static void applyAdminUpdate(Event e, UpdateEventAdminRequest dto) {
        if (dto.getAnnotation() != null) e.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getTitle() != null) e.setTitle(dto.getTitle());
        if (dto.getCategory() != null) e.setCategoryId(dto.getCategory());
        if (dto.getEventDate() != null) e.setEventDate(dto.getEventDate());
        if (dto.getLocation() != null) {
            e.setLocationLat(dto.getLocation().getLat());
            e.setLocationLon(dto.getLocation().getLon());
        }
        if (dto.getPaid() != null) e.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) e.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) e.setRequestModeration(dto.getRequestModeration());
    }

    public static EventFullDto toFullFromDto(EventFullDto dto,
                                             long views,
                                             long confirmed,
                                             RatingDto rating) {

        return EventFullDto.builder()
                .id(dto.getId())
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .confirmedRequests(confirmed)
                .eventDate(dto.getEventDate())
                .initiator(dto.getInitiator())
                .location(dto.getLocation())
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .state(dto.getState())
                .createdOn(dto.getCreatedOn())
                .publishedOn(dto.getPublishedOn())
                .title(dto.getTitle())
                .views(views)
                .rating(rating)
                .build();
    }

    public static EventShortDto toShortFromDto(EventFullDto dto,
                                               long views,
                                               long confirmed,
                                               RatingDto rating) {

        return EventShortDto.builder()
                .id(dto.getId())
                .annotation(dto.getAnnotation())
                .category(dto.getCategory())
                .confirmedRequests(confirmed)
                .eventDate(dto.getEventDate())
                .initiator(dto.getInitiator())
                .paid(dto.getPaid())
                .title(dto.getTitle())
                .views(views)
                .rating(rating)
                .build();
    }
}