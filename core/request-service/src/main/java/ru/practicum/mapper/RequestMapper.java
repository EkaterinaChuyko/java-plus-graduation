package ru.practicum.mapper;

import ru.practicum.request.ParticipationRequest;
import ru.practicum.request.ParticipationRequestDto;

public class RequestMapper {

    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus().toString())
                .build();
    }
}