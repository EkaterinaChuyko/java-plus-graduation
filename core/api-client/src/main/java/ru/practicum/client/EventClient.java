package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.request.UpdateEventAdminRequest;
import ru.practicum.request.UpdateEventUserRequest;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "event-service")
public interface EventClient {

    @PostMapping("/internal/events")
    EventFullDto createEvent(@RequestParam("userId") Long userId,
                             @RequestBody NewEventDto dto);

    @GetMapping("/internal/events/{eventId}")
    EventFullDto getEvent(@PathVariable("eventId") Long eventId);

    @GetMapping("/internal/events/{eventId}/short")
    EventShortDto getEventShort(@PathVariable("eventId") Long eventId);

    @GetMapping("/internal/events/{eventId}/exists")
    Boolean eventExists(@PathVariable("eventId") Long eventId);

    @GetMapping("/internal/events/user/{userId}")
    List<EventShortDto> getUserEvents(@PathVariable("userId") Long userId,
                                     @RequestParam("from") int from,
                                     @RequestParam("size") int size);

    @GetMapping("/internal/events/search/public")
    List<EventShortDto> searchPublic(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam int from,
            @RequestParam int size
    );

    @GetMapping("/internal/events/search/admin")
    List<EventFullDto> searchAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam int from,
            @RequestParam int size
    );

    @PutMapping("/internal/events/{eventId}/admin")
    EventFullDto updateByAdmin(@PathVariable Long eventId,
                               @RequestBody UpdateEventAdminRequest dto);

    @PutMapping("/internal/events/{eventId}/user")
    EventFullDto updateByUser(@RequestParam Long userId,
                              @PathVariable Long eventId,
                              @RequestBody UpdateEventUserRequest dto);
}