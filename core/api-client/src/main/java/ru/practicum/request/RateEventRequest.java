package ru.practicum.request;

import lombok.*;
import jakarta.validation.constraints.NotNull;

@Data
public class RateEventRequest {
    @NotNull
    private Long eventId;

    @NotNull
    private Boolean isLike;
}