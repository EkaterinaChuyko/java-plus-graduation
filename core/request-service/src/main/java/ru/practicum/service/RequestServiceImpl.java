package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.enums.Status;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.repository.RequestRepository;
import ru.practicum.request.EventRequestStatusUpdateRequest;
import ru.practicum.request.EventRequestStatusUpdateResult;
import ru.practicum.request.ParticipationRequest;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.enums.RequestStatus;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestCircuitBreakerService circuitBreakerService;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        if (!Boolean.TRUE.equals(circuitBreakerService.userExists(userId))) {
            throw new NotFoundException("User not found");
        }

        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        if (!Boolean.TRUE.equals(circuitBreakerService.userExists(userId))) {
            throw new NotFoundException("User not found");
        }

        EventShortDto event;
        try {
            event = circuitBreakerService.getEventById(eventId);
        } catch (Exception e) {
            log.error("Failed to fetch event {}: {}", eventId, e.getMessage());
            throw new NotFoundException("Event not found");
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Repeated request");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot participate");
        }

        if (!"PUBLISHED".equals(event.getState())) {
            throw new ConflictException("Event not published");
        }

        if (event.getParticipantLimit() != null && event.getParticipantLimit() > 0) {
            long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmed >= event.getParticipantLimit()) {
                throw new ConflictException("Participant limit reached");
            }
        }

        RequestStatus status = RequestStatus.PENDING;
        if ((event.getRequestModeration() == null || !event.getRequestModeration())
            || (event.getParticipantLimit() != null && event.getParticipantLimit() == 0)) {
            status = RequestStatus.CONFIRMED;
        }

        ParticipationRequest req = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .requesterId(userId)
                .eventId(eventId)
                .status(status)
                .build();

        log.info("Creating request: user {} for event {}, status {}", userId, eventId, status);
        return RequestMapper.toDto(requestRepository.save(req));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (!req.getRequesterId().equals(userId)) {
            throw new NotFoundException("Request doesn't belong to user");
        }

        req.setStatus(RequestStatus.CANCELED);
        log.info("Cancelling request {} for user {}", requestId, userId);
        return RequestMapper.toDto(requestRepository.save(req));
    }

    @Override
    public Long getConfirmedRequestsCount(Long eventId) {
        log.debug("Internal API: get confirmed requests count for event {}", eventId);
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEventId(Long eventId) {
        log.debug("Internal API: get all requests for event {}", eventId);
        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long eventId, EventRequestStatusUpdateRequest request) {
        log.debug("Internal API: update request statuses for event {}", eventId);

        if (!Boolean.TRUE.equals(circuitBreakerService.eventExists(eventId))) {
            throw new NotFoundException("Event not found");
        }

        EventShortDto event;
        try {
            event = circuitBreakerService.getEventById(eventId);
        } catch (Exception e) {
            log.error("Failed to fetch event {}: {}", eventId, e.getMessage());
            throw new NotFoundException("Event not found");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(request.getRequestIds());

        for (ParticipationRequest req : requests) {
            if (!req.getEventId().equals(eventId)) {
                throw new ConflictException("Request " + req.getId() + " does not belong to event " + eventId);
            }
        }

        if (requests.isEmpty()) {
            throw new NotFoundException("Requests not found");
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() != null && event.getParticipantLimit() > 0
            && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("The participant limit has been reached");
        }

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest req : requests) {
            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request " + req.getId() + " must be PENDING");
            }

            if (request.getStatus() == Status.CONFIRMED) {
                if (event.getParticipantLimit() != null
                    && event.getParticipantLimit() > 0
                    && confirmedCount >= event.getParticipantLimit()) {

                    req.setStatus(RequestStatus.REJECTED);
                    rejected.add(RequestMapper.toDto(requestRepository.save(req)));

                } else {
                    req.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(RequestMapper.toDto(requestRepository.save(req)));
                    confirmedCount++;
                }
            } else {
                req.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toDto(requestRepository.save(req)));
            }
        }

        log.info("Updated request statuses for event {}: confirmed={}, rejected={}",
                eventId, confirmed.size(), rejected.size());

        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }
}