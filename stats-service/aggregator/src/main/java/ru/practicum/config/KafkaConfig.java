package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kafka")
public class KafkaConfig {
    private String server;
    private Topics topics;
    private Consumer consumer;

    @Getter
    @Setter
    public static class Topics {
        private String users;
        private String events;
    }

    @Getter
    @Setter
    public static class Consumer {
        private String clientId;
        private String groupId;
        private Integer pollDurationMs;
    }
}
