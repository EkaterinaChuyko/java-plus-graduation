package ru.practicum.runner;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.service.EventSimilarityConsumer;
import ru.practicum.service.UserActionConsumer;

@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {
    private final UserActionConsumer userActionConsumer;
    private final EventSimilarityConsumer eventSimilarityConsumer;

    @Override
    public void run(String... args) throws Exception {
        Thread userActionsThread = new Thread(userActionConsumer);
        userActionsThread.setName("UserActionConsumerThread");
        userActionsThread.start();

        eventSimilarityConsumer.start();
    }
}