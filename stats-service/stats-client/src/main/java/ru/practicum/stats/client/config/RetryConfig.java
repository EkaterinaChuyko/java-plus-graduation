package ru.practicum.stats.client.config;

import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
public class RetryConfig {

    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy backOff = new FixedBackOffPolicy();
        backOff.setBackOffPeriod(3000L);

        MaxAttemptsRetryPolicy policy = new MaxAttemptsRetryPolicy();
        policy.setMaxAttempts(3);

        retryTemplate.setBackOffPolicy(backOff);
        retryTemplate.setRetryPolicy(policy);

        return retryTemplate;
    }
}
