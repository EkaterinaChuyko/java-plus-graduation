package ru.practicum.request;

import lombok.Data;
import ru.practicum.dto.enums.Status;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private Status status;
}