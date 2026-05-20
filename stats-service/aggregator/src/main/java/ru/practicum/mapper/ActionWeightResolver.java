package ru.practicum.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.config.ActionWeightsConfig;
import ru.practicum.stats.avro.ActionTypeAvro;

@Component
@RequiredArgsConstructor
public class ActionWeightResolver {

    private final ActionWeightsConfig config;

    public double resolve(ActionTypeAvro type) {

        return switch (type) {
            case ACTION_VIEW -> config.getView();
            case ACTION_REGISTER -> config.getRegister();
            case ACTION_LIKE -> config.getLike();
        };
    }
}
