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
import ru.practicum.service.ActionService;
import ru.practicum.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActionProcessor implements Runnable {
    private final KafkaClient kafkaClient;
    private final ActionService service;

    @Value("${topics.user-actions}")
    private String topic;

    @Override
    public void run() {
        log.info("Starting ActionProcessor for topic '{}'", topic);

        Consumer<String, UserActionAvro> consumer = kafkaClient.getConsumerUserAction();
        consumer.subscribe(List.of(topic));

        try {
            while (true) {
                ConsumerRecords<String, UserActionAvro> records =
                        consumer.poll(Duration.ofSeconds(5));

                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    log.info("Received message {}", record.value());
                    service.saveOrUpdate(record.value());
                }

                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            }

        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("Error while processing Kafka events", e);
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
