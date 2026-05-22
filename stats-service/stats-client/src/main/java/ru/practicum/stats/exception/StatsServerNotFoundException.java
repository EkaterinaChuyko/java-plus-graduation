package ru.practicum.stats.exception;

public class StatsServerNotFoundException extends RuntimeException {
    public StatsServerNotFoundException(String message) {
        super(message);
    }
}