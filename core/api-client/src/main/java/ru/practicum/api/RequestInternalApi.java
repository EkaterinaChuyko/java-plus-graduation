package ru.practicum.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.request.EventRequestStatusUpdateRequest;
import ru.practicum.request.EventRequestStatusUpdateResult;

import java.util.List;

public interface RequestInternalApi {

    @GetMapping("/internal/requests/count/{eventId}")
    Long getConfirmedRequestsCount(@PathVariable("eventId") Long eventId);

    @GetMapping("/internal/requests/event/{eventId}")
    List<ParticipationRequestDto> getRequestsByEventId(@PathVariable("eventId") Long eventId);

    @PutMapping("/internal/requests/event/{eventId}/status")
    EventRequestStatusUpdateResult updateRequestStatus(
            @PathVariable("eventId") Long eventId,
            @RequestBody EventRequestStatusUpdateRequest request);
}
