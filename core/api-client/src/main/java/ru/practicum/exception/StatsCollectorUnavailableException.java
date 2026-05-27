package ru.practicum.exception;

public class StatsCollectorUnavailableException extends RuntimeException {

    public StatsCollectorUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public StatsCollectorUnavailableException(String message) {
        super(message);
    }
}
