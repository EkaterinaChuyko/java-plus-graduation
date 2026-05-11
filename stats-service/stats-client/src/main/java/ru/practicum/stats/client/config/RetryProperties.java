package ru.practicum.stats.client.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "stats.client.retry")
@Validated
@Getter
@Setter
public class RetryProperties {

    @Min(1)
    private int maxAttempts = 3;

    @NotNull
    private RetryType type = RetryType.FIXED;

    @Valid
    private Fixed fixed = new Fixed();

    @Valid
    private Exponential exponential = new Exponential();

    public enum RetryType {
        FIXED,
        EXPONENTIAL
    }

    @Getter
    @Setter
    public static class Fixed {
        @Min(0)
        private long delay = 3000;
    }

    @Getter
    @Setter
    public static class Exponential {

        @Min(1)
        private long initialDelay = 1000;

        @DecimalMin("1.0")
        private double multiplier = 2.0;

        @Min(1)
        private long maxDelay = 10000;

        private boolean jitter = false;
    }
}
