package ru.practicum.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.client.UserClient;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class UserClientFallback implements UserClient {

    @Override
    public UserDto getUserById(Long userId) {
        log.warn("Fallback: getUserById({})", userId);
        return null;
    }

    @Override
    public UserShortDto getUserShortById(Long userId) {
        log.warn("Fallback: getUserShortById({})", userId);
        return null;
    }

    @Override
    public Boolean userExists(Long userId) {
        log.warn("Fallback: userExists({}) -> false", userId);
        return false;
    }

    @Override
    public List<UserShortDto> getUsersByIds(Set<Long> ids) {
        log.warn("Fallback: getUsersByIds({}) -> empty list", ids);
        return List.of();
    }
}
