package ru.practicum.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.properties.KafkaTopicsProperties;
import ru.practicum.service.AggregationService;
import ru.practicum.stats.avro.UserActionAvro;


@Component
@RequiredArgsConstructor
@Slf4j
public class UserActionConsumer {

    private final AggregationService aggregationService;
    private final KafkaTopicsProperties topics;

    @KafkaListener(
            topics = "#{@kafkaTopicsProperties.users}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(UserActionAvro action) {
        log.info("Received action: {}", action);
        aggregationService.process(action);
    }
}
