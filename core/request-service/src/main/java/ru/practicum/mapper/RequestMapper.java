package ru.practicum.mapper;

import ru.practicum.request.ParticipationRequest;
import ru.practicum.dto.request.ParticipationRequestDto;

public class RequestMapper {

    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        if (request == null) {
            return null;
        }

        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus().name())
                .build();
    }
}