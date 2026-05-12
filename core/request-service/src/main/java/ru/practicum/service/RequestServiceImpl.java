package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ConflictException;
import ru.practicum.NotFoundException;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.event.dto.event.EventFullDto;
import ru.practicum.event.dto.event.EventState;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.repository.RequestRepository;
import ru.practicum.request.ParticipationRequest;
import ru.practicum.request.ParticipationRequestDto;
import ru.practicum.request.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {

        Boolean userExists = userClient.existsById(userId);

        if (Boolean.FALSE.equals(userExists)) {
            throw new NotFoundException("User not found");
        }

        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {

        Boolean userExists = userClient.existsById(userId);

        if (Boolean.FALSE.equals(userExists)) {
            throw new NotFoundException("User not found");
        }

        EventFullDto event = eventClient.getEvent(eventId);

        if (event == null) {
            throw new NotFoundException("Event not found");
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Repeated request");
        }

        if (event.getInitiator() != null
            && event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot participate");
        }

        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new ConflictException("Event not published");
        }

        if (event.getParticipantLimit() != null
            && event.getParticipantLimit() > 0) {

            long confirmed = requestRepository.countByEventIdAndStatus(
                    eventId,
                    RequestStatus.CONFIRMED
            );

            if (confirmed >= event.getParticipantLimit()) {
                throw new ConflictException("Limit reached");
            }
        }

        RequestStatus status = RequestStatus.PENDING;

        if (Boolean.FALSE.equals(event.getRequestModeration())
            || event.getParticipantLimit() == null
            || event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        }

        ParticipationRequest req = new ParticipationRequest();
        req.setCreated(LocalDateTime.now());
        req.setRequesterId(userId);
        req.setEventId(eventId);
        req.setStatus(status);

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

        if (req.getStatus() != RequestStatus.PENDING) {
            throw new ConflictException("Only pending requests can be canceled");
        }

        req.setStatus(RequestStatus.CANCELED);

        return RequestMapper.toDto(requestRepository.save(req));
    }
}