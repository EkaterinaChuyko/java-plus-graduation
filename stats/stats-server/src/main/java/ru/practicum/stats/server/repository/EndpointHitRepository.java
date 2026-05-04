package ru.practicum.stats.server.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.server.model.EndpointHit;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends CrudRepository<EndpointHit, Long> {

    @Query("""
        SELECT new ru.practicum.stats.dto.ViewStatsDto(
            e.app,
            e.uri,
            COUNT(e.id) * 1L
        )
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
        GROUP BY e.app, e.uri
        ORDER BY 3 DESC
    """)
    List<ViewStatsDto> getStatsTotalAll(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT new ru.practicum.stats.dto.ViewStatsDto(
            e.app,
            e.uri,
            COUNT(e.id) * 1L
        )
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
          AND e.uri IN :uris
        GROUP BY e.app, e.uri
        ORDER BY 3 DESC
    """)
    List<ViewStatsDto> getStatsTotal(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris
    );

    @Query("""
        SELECT new ru.practicum.stats.dto.ViewStatsDto(
            e.app,
            e.uri,
            COUNT(DISTINCT e.ip) * 1L
        )
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
        GROUP BY e.app, e.uri
        ORDER BY 3 DESC
    """)
    List<ViewStatsDto> getStatsUniqueAll(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT new ru.practicum.stats.dto.ViewStatsDto(
            e.app,
            e.uri,
            COUNT(DISTINCT e.ip) * 1L
        )
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
          AND e.uri IN :uris
        GROUP BY e.app, e.uri
        ORDER BY 3 DESC
    """)
    List<ViewStatsDto> getStatsUnique(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris
    );
}