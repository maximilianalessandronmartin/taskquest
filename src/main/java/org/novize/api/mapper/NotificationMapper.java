package org.novize.api.mapper;

import lombok.RequiredArgsConstructor;

import org.novize.api.dtos.NotificationDto;
import org.novize.api.model.Notification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationMapper {
    private final UserMapper userMapper;

    public NotificationDto toDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .recipient(userMapper.toDto(notification.getRecipient()))
                .type(notification.getType())
                .message(notification.getMessage())
                .payload(notification.getPayload())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public List<NotificationDto> toDtoList(List<Notification> notifications) {
        return notifications.stream()
                .map(this::toDto)
                .toList();
    }
}