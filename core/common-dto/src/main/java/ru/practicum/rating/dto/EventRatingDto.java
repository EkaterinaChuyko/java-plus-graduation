package ru.practicum.rating.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRatingDto {
    private Long id;
    private Long userId;
    private Long eventId;
    private Boolean isLike;
    private LocalDateTime created;
}