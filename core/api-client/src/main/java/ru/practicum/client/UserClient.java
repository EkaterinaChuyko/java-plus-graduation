package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/internal/users/{userId}")
    UserDto getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/internal/users")
    Map<Long, UserDto> getUsersByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/internal/users/{userId}/exists")
    Boolean existsById(@PathVariable("userId") Long userId);

    @GetMapping("/internal/users/{userId}/short")
    UserShortDto getUserShortById(
            @PathVariable("userId") Long userId
    );
}