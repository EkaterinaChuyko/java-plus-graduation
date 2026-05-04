package ru.practicum.stats.client.impl;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;
import ru.practicum.stats.client.config.RetryConfig;
import ru.practicum.stats.client.discovery.StatsServiceDiscovery;

import java.net.URI;

@Component
public class StatsUriBuilder {

    private final StatsServiceDiscovery discovery;
    private final RetryConfig retryConfig;

    public StatsUriBuilder(StatsServiceDiscovery discovery, RetryConfig retryConfig) {
        this.discovery = discovery;
        this.retryConfig = retryConfig;
    }

    public URI makeUri(String path) {
        ServiceInstance instance = retryConfig
                .retryTemplate()
                .execute(ctx -> discovery.getInstance());

        return URI.create(
                "http://" + instance.getHost() + ":" + instance.getPort() + path
        );
    }
}