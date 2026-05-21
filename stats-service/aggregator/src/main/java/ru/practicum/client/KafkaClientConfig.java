package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Slf4j
@Configuration
public class KafkaClientConfig {

    @Bean
    public KafkaClient getClient() {
        return new KafkaClient() {

            @Value("${spring.kafka.bootstrap-servers}")
            private String bootstrapServers;

            @Value("${spring.kafka.producer.key-serializer}")
            private String keySerializer;

            @Value("${spring.kafka.producer.value-serializer}")
            private String valueSerializer;

            @Value("${spring.kafka.consumer.key-deserializer}")
            private String keyDeserializer;

            @Value("${spring.kafka.consumer.value-deserializer}")
            private String valueDeserializer;

            @Value("${spring.kafka.consumer.group-id}")
            private String idGroup;

            @Value("${spring.kafka.properties.schema.registry.url}")
            private String schemaRegistryUrl;

            private Producer<String, SpecificRecordBase> producer;
            private Consumer<String, SpecificRecordBase> consumer;

            @Override
            public Producer<String, SpecificRecordBase> getProducer() {
                if (producer == null) {
                    initProducer();
                }

                log.debug("Connecting producer to Kafka at {}", bootstrapServers);

                return producer;
            }

            private void initProducer() {
                Properties config = new Properties();

                config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
                config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);

                config.put("schema.registry.url", schemaRegistryUrl);

                producer = new KafkaProducer<>(config);
            }

            @Override
            public Consumer<String, SpecificRecordBase> getConsumer() {
                if (consumer == null) {
                    initConsumer();
                }

                log.debug("Connecting consumer to Kafka at {}", bootstrapServers);

                return consumer;
            }

            private void initConsumer() {
                Properties config = new Properties();

                config.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                config.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
                config.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
                config.setProperty(ConsumerConfig.GROUP_ID_CONFIG, idGroup);

                config.setProperty("schema.registry.url", schemaRegistryUrl);
                config.setProperty("specific.avro.reader", "true");

                consumer = new KafkaConsumer<>(config);
            }

            @Override
            public void stop() {
                if (producer != null) {
                    producer.close();
                }

                if (consumer != null) {
                    consumer.close();
                }
            }
        };
    }
}