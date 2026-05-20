package ru.practicum.stats.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableConfigurationProperties(RetryProperties.class)
public class RetryConfig {

    @Bean
    public RetryTemplate retryTemplate(RetryProperties properties) {
        RetryTemplate template = new RetryTemplate();

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(properties.getMaxAttempts());
        template.setRetryPolicy(retryPolicy);

        switch (properties.getType()) {
            case EXPONENTIAL -> {
                ExponentialBackOffPolicy policy = new ExponentialBackOffPolicy();
                policy.setInitialInterval(properties.getExponential().getInitialDelay());
                policy.setMultiplier(properties.getExponential().getMultiplier());
                policy.setMaxInterval(properties.getExponential().getMaxDelay());

                template.setBackOffPolicy(policy);
            }
            case FIXED -> {
                FixedBackOffPolicy policy = new FixedBackOffPolicy();
                policy.setBackOffPeriod(properties.getFixed().getDelay());

                template.setBackOffPolicy(policy);
            }
        }
        return template;
    }
}
