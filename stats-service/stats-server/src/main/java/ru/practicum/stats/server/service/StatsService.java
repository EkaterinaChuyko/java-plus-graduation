package ru.practicum.stats.server.service;

import ru.practicum.dto.stats.HitDto;
import ru.practicum.dto.stats.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void saveHit(HitDto dto);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}