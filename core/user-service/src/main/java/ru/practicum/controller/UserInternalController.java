package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.service.UserService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Slf4j
public class UserInternalController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/batch")
    public List<UserShortDto> getUsersByIds(@RequestBody Set<Long> ids) {
        return userService.getUsersByIds(ids);
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return userMapper.toDto(user);
    }

    @GetMapping("/{userId}/short")
    public UserShortDto getUserShortById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return new UserShortDto(user.getId(), user.getName());
    }

    @GetMapping("/exists/{userId}")
    public Boolean userExists(@PathVariable Long userId) {
        return userService.existsById(userId);
    }
}