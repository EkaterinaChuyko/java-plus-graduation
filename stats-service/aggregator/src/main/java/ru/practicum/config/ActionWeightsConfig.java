package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "action-weights")
public class ActionWeightsConfig {

    private double view;
    private double register;
    private double like;
}
