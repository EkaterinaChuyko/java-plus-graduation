package ru.practicum.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDto> confirmedRequests;
    private List<ParticipationRequestDto> rejectedRequests;
}