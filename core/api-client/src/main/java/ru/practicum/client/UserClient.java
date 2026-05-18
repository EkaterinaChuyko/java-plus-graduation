package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.fallback.UserClientFallback;

import java.util.List;
import java.util.Set;

@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/internal/users/{userId}")
    UserDto getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/internal/users/{userId}/short")
    UserShortDto getUserShortById(@PathVariable("userId") Long userId);

    @GetMapping("/internal/users/exists/{userId}")
    Boolean userExists(@PathVariable("userId") Long userId);

    @PostMapping("/internal/users/batch")
    List<UserShortDto> getUsersByIds(@RequestBody Set<Long> ids);
}