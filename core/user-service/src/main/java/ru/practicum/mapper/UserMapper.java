package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.model.User;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UpdateUserRequest;
import ru.practicum.user.dto.UserDto;

@UtilityClass
public class UserMapper {

    public User toEntity(NewUserRequest dto) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public UserDto toDto(User entity) {
        return UserDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .build();
    }

    public User updateFromDto(User user, UpdateUserRequest dto) {
        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            user.setEmail(dto.getEmail());
        }
        return user;
    }
}