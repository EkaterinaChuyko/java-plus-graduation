package ru.practicum.client;

import org.apache.kafka.clients.consumer.Consumer;
import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.avro.UserActionAvro;

public interface KafkaClient {

    Consumer<String, EventSimilarityAvro> getConsumerSimilarity();

    Consumer<String, UserActionAvro> getConsumerUserAction();

    void stop();
}
