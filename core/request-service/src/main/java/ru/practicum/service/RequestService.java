package ru.practicum.service;

import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.request.EventRequestStatusUpdateRequest;
import ru.practicum.request.EventRequestStatusUpdateResult;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto addRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    Long getConfirmedRequestsCount(Long eventId);

    List<ParticipationRequestDto> getRequestsByEventId(Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(Long eventId, EventRequestStatusUpdateRequest request);
}