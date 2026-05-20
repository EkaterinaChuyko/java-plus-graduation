package ru.practicum.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class EventPair {

    private final long first;
    private final long second;

    public EventPair(long a, long b) {
        this.first = Math.min(a, b);
        this.second = Math.max(a, b);
    }
}
