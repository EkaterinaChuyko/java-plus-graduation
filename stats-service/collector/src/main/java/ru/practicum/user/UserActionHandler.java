package ru.practicum.user;

import ru.practicum.stats.proto.user.UserActionProto;

public interface UserActionHandler {
    void handle(UserActionProto event);
}
