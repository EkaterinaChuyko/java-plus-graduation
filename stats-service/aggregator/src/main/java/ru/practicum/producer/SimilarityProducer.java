package ru.practicum.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.config.KafkaConfig;
import ru.practicum.stats.avro.EventSimilarityAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimilarityProducer {

    private final KafkaTemplate<Long, EventSimilarityAvro> kafkaTemplate;
    private final KafkaConfig kafkaConfig;

    public void send(EventSimilarityAvro similarity) {

        kafkaTemplate.send(
                kafkaConfig.getTopics().getEvents(),
                similarity.getEventA(),
                similarity
        ).whenComplete((res, ex) -> {
            if (ex != null) {
                log.error("Error sending similarity", ex);
            }
        });
    }
}