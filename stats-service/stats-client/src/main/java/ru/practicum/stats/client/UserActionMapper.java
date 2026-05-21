package ru.practicum.stats.client;

import com.google.protobuf.Timestamp;
import ru.practicum.stats.proto.user.ActionTypeProto;
import ru.practicum.stats.proto.user.UserActionProto;

import java.time.Instant;

public class UserActionMapper {

    public static UserActionProto toProto(Long userId, Long eventId, ActionTypeProto actionType, Instant timestamp) {
        return UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(timestamp.getEpochSecond())
                        .setNanos(timestamp.getNano())
                        .build())
                .build();
    }
}
