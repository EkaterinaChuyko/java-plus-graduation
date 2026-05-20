package ru.practicum.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
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

        ProducerRecord<Long, EventSimilarityAvro> record =
                new ProducerRecord<>(
                        kafkaConfig.getTopics().getEvents(),
                        similarity.getEventA(),
                        similarity
                );

        kafkaTemplate.send(record)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.error("Error sending similarity", ex);
                    }
                });
    }
}