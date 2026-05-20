package ru.practicum.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.service.EventSimilarityConsumer;
import ru.practicum.service.UserActionConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {

    private final UserActionConsumer userActionConsumer;
    private final EventSimilarityConsumer eventSimilarityConsumer;

    @Override
    public void run(String... args) {

        Thread userActionsThread = new Thread(userActionConsumer);
        userActionsThread.setName("UserActionConsumerThread");
        userActionsThread.setDaemon(true);
        userActionsThread.start();

        Thread eventSimilarityThread =
                new Thread(eventSimilarityConsumer::start);

        eventSimilarityThread.setName("EventSimilarityConsumerThread");
        eventSimilarityThread.setDaemon(true);
        eventSimilarityThread.start();

        log.info("Kafka consumers successfully started");
    }
}