package org.novize.api.mapper;

import lombok.RequiredArgsConstructor;
import org.novize.api.dtos.user.UserDto;
import org.novize.api.model.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    public UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .createdAt(user.getCreatedAt())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .username(user.getUsername())
                .email(user.getEmail())
                .xp(user.getXp())
                .build();
    }


}
