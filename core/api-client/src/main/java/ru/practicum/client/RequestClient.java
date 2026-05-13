package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.request.EventRequestStatusUpdateRequest;
import ru.practicum.request.EventRequestStatusUpdateResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

@FeignClient(name = "request-service")
public interface RequestClient {

    @GetMapping("/internal/requests/event/{eventId}")
    List<ParticipationRequestDto> getEventRequests(
            @PathVariable("eventId") Long eventId
    );

    @PatchMapping("/internal/requests/event/{eventId}/status")
    EventRequestStatusUpdateResult updateRequestStatus(
            @PathVariable("eventId") Long eventId,
            @RequestBody EventRequestStatusUpdateRequest request
    );

    @GetMapping("/internal/requests")
    ParticipationRequestDto getUserRequest(
            @RequestParam("userId") Long userId,
            @RequestParam("eventId") Long eventId
    );

    @PostMapping("/internal/requests")
    ParticipationRequestDto createRequest(
            @RequestParam("userId") Long userId,
            @RequestParam("eventId") Long eventId
    );

    @PatchMapping("/internal/requests/{requestId}/cancel")
    ParticipationRequestDto cancelRequest(
            @PathVariable("requestId") Long requestId,
            @RequestParam("userId") Long userId
    );

    @GetMapping("/internal/requests/user/{userId}")
    List<ParticipationRequestDto> getUserRequests(
            @PathVariable("userId") Long userId
    );

    @GetMapping("/internal/requests/event/{eventId}/confirmed/count")
    Long getConfirmedRequestsCount(
            @PathVariable("eventId") Long eventId
    );

    @GetMapping("/internal/requests/events/confirmed/count")
    Map<Long, Long> getConfirmedRequestsCounts(
            @RequestParam("eventIds") Set<Long> eventIds
    );

    @GetMapping("/internal/requests/confirmed")
    Boolean hasConfirmedRequest(
            @RequestParam("userId") Long userId,
            @RequestParam("eventId") Long eventId
    );
}