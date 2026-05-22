package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.stats.HitDto;
import ru.practicum.dto.stats.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Slf4j
@Component
public class StatsClient {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;

    private final String statsServiceId = "stats-server";

    public StatsClient(DiscoveryClient discoveryClient,
                       RestTemplate restTemplate) {

        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplate;

        this.retryTemplate = new RetryTemplate();

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(100L);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(2);
        retryTemplate.setRetryPolicy(retryPolicy);
    }

    private String resolveBaseUrl() {

        return retryTemplate.execute(context -> {

            List<ServiceInstance> instances =
                    discoveryClient.getInstances(statsServiceId);

            if (instances == null || instances.isEmpty()) {

                log.warn("No instances in Eureka for {}", statsServiceId);

                throw new IllegalStateException(
                        "Stats service unavailable: " + statsServiceId
                );
            }

            ServiceInstance instance = instances.get(0);

            String baseUrl =
                    "http://" + instance.getHost() + ":" + instance.getPort();

            log.debug("Resolved stats-server URL: {}", baseUrl);

            return baseUrl;
        });
    }

    public void hit(HitDto hitDto) {

        try {

            String baseUrl = resolveBaseUrl();

            log.debug("Sending hit to stats-server: {}", hitDto);

            restTemplate.postForEntity(
                    baseUrl + "/hit",
                    hitDto,
                    Void.class
            );

        } catch (Exception e) {
            log.error("Could not save stats hit", e);
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       boolean unique) {

        try {

            String baseUrl = resolveBaseUrl();

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/stats")
                    .queryParam(
                            "start",
                            start.format(DATE_TIME_FORMATTER)
                    )
                    .queryParam(
                            "end",
                            end.format(DATE_TIME_FORMATTER)
                    )
                    .queryParam("unique", unique);

            if (uris != null && !uris.isEmpty()) {

                for (String uri : uris) {
                    builder.queryParam("uris", uri);
                }
            }

            String url = builder.toUriString();

            log.debug("STATS REQUEST URL: {}", url);

            ViewStatsDto[] response =
                    restTemplate.getForObject(
                            url,
                            ViewStatsDto[].class
                    );

            return response == null
                    ? Collections.emptyList()
                    : Arrays.asList(response);

        } catch (Exception e) {
            log.error("Failed to fetch stats", e);

            return Collections.emptyList();
        }
    }
}