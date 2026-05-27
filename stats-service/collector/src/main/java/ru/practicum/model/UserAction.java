package ru.practicum.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Builder
public class UserAction {

    @NotNull
    private Long userId;

    @NotNull
    private Long eventId;

    @NotNull
    private ActionType actionType;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
