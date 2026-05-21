package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.client.KafkaClient;
import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaClient kafkaClient;
    private final SimilarityService similarityService;

    @Value("${topics.user-actions}")
    private String userActionsTopic;

    @Value("${topics.events-similarity}")
    private String eventsSimilarityTopic;

    public void start() {
        log.info("Starting aggregation service");

        Consumer<String, SpecificRecordBase> consumer = kafkaClient.getConsumer();
        Producer<String, SpecificRecordBase> producer = kafkaClient.getProducer();

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            log.info("Subscribing to topic: {}", userActionsTopic);
            consumer.subscribe(List.of(userActionsTopic));

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records =
                        consumer.poll(Duration.ofSeconds(5));
                if (records.isEmpty()) {
                    continue;
                }
                for (ConsumerRecord<String, SpecificRecordBase> record : records) {

                    if (!(record.value() instanceof UserActionAvro action)) {
                        log.warn("Skipping unknown message type: {}", record.value());
                        continue;
                    }

                    List<EventSimilarityAvro> similarities =
                            similarityService.updateSimilarity(action);

                    log.info("Received message: {}", action);

                    for (EventSimilarityAvro similarity : similarities) {
                        producer.send(new ProducerRecord<>(
                                eventsSimilarityTopic,
                                similarity
                        ));

                        log.info(
                                "Sent similarity to Kafka: events=({}, {}), score={}",
                                similarity.getEventA(),
                                similarity.getEventB(),
                                similarity.getScore()
                        );
                    }
                }
                consumer.commitAsync();
            }

        } catch (WakeupException ignored) {
            log.info("Consumer shutdown triggered (WakeupException)");
        } catch (Exception e) {
            log.error("Unexpected error in aggregation loop", e);
        } finally {
            try {
                producer.flush();
            } finally {
                log.info("Closing consumer");
                consumer.close();

                log.info("Closing producer");
                producer.close();
            }
        }
    }
}
