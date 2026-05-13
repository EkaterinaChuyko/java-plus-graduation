package ru.practicum.stats.client;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.stats.HitDto;
import ru.practicum.dto.stats.ViewStatsDto;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Component
public class StatsClient {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    public StatsClient(DiscoveryClient discoveryClient,
                       RestTemplate restTemplate) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplate;
    }

    private String getBaseUrl() {
        ServiceInstance instance = discoveryClient
                .getInstances("stats-server")
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("stats-server unavailable"));

        return "http://" + instance.getHost() + ":" + instance.getPort();
    }

    public void saveHit(HitDto dto) {
        restTemplate.postForEntity(
                getBaseUrl() + "/hit",
                dto,
                Void.class
        );
    }

    public List<ViewStatsDto> getStats(
            String start,
            String end,
            String[] uris,
            boolean unique
    ) {

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(getBaseUrl() + "/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("unique", unique);

        if (uris != null) {
            builder.queryParam("uris", (Object[]) uris);
        }

        ViewStatsDto[] response = restTemplate.getForObject(
                builder.toUriString(),
                ViewStatsDto[].class
        );

        return response == null
                ? Collections.emptyList()
                : Arrays.asList(response);
    }
}