package ru.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.avro.UserActionAvro;
import ru.practicum.stats.proto.user.ActionTypeProto;
import ru.practicum.stats.proto.user.UserActionProto;

import java.time.Instant;

@Slf4j
@Component
public class UserActionMapper {

    public UserActionAvro toAvro(UserActionProto action) {
        log.info("Mapping user action to Avro format");

        Instant timestamp = Instant.ofEpochSecond(
                action.getTimestamp().getSeconds(),
                action.getTimestamp().getNanos()
        );

        return UserActionAvro.newBuilder()
                .setUserId(action.getUserId())
                .setEventId(action.getEventId())
                .setTimestamp(timestamp)
                .setActionType(toActionType(action.getActionType()))
                .build();
    }

    public static ActionTypeAvro toActionType(ActionTypeProto actionTypeProto) {
        if (actionTypeProto == null) {
            throw new IllegalArgumentException("ActionTypeProto is null");
        }

        return switch (actionTypeProto) {
            case ACTION_VIEW -> ActionTypeAvro.ACTION_VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.ACTION_REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.ACTION_LIKE;
            default -> throw new IllegalArgumentException(
                    "Unsupported ActionTypeProto: " + actionTypeProto
            );
        };
    }
}