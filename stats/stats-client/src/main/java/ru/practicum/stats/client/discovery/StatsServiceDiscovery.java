package ru.practicum.stats.client.discovery;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

@Component
public class StatsServiceDiscovery {

    private final DiscoveryClient discoveryClient;
    private final String statsServiceId = "STATS-SERVER";

    public StatsServiceDiscovery(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public ServiceInstance getInstance() {
        try {
            return discoveryClient
                    .getInstances(statsServiceId)
                    .stream()
                    .findFirst()
                    .orElseThrow();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Stats service not found: " + statsServiceId, e
            );
        }
    }
}
