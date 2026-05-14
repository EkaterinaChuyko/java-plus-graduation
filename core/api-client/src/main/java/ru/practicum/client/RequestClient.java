package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.request.EventRequestStatusUpdateRequest;
import ru.practicum.request.EventRequestStatusUpdateResult;

import java.util.List;

@FeignClient(name = "request-service")
public interface RequestClient {

    @GetMapping("/internal/requests/count/{eventId}")
    Long getConfirmedRequestsCount(@PathVariable("eventId") Long eventId);

    @GetMapping("/internal/requests/event/{eventId}")
    List<ParticipationRequestDto> getRequestsByEventId(@PathVariable("eventId") Long eventId);

    @PutMapping("/internal/requests/event/{eventId}/status")
    EventRequestStatusUpdateResult updateRequestStatus(
            @PathVariable("eventId") Long eventId,
            @RequestBody EventRequestStatusUpdateRequest request);

    @GetMapping("/internal/requests/exists")
    Boolean hasConfirmedRequest(@RequestParam("userId") Long userId,
                                @RequestParam("eventId") Long eventId);
}