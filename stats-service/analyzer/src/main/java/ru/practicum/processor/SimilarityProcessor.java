package ru.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.client.KafkaClient;
import ru.practicum.service.SimilarityService;
import ru.practicum.stats.avro.EventSimilarityAvro;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityProcessor implements Runnable {

    private final KafkaClient kafkaClient;
    private final SimilarityService service;

    @Value("${topics.events-similarity}")
    private String topic;

    @Override
    public void run() {
        log.info("Starting SimilarityProcessor for topic '{}'", topic);
        Consumer<String, EventSimilarityAvro> consumer = kafkaClient.getConsumerSimilarity();
        consumer.subscribe(List.of(topic));
        try {
            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer.poll(Duration.ofSeconds(5));
                if (records.isEmpty()) {
                    continue;
                }
                List<EventSimilarityAvro> batch = new ArrayList<>();
                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {

                    log.info("Received message {}", record.value());

                    batch.add(record.value());
                }
                service.saveOrUpdateBatch(batch);
                log.info("Processed {} similarity messages", batch.size());
                consumer.commitSync();
            }

        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("Error while processing similarity messages", e);

        } finally {

            try {
                consumer.commitSync();
            } catch (Exception ignored) {
            }
            consumer.close();
            log.info("Consumer closed");
        }
    }
}