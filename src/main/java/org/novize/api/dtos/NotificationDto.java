package org.novize.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.novize.api.dtos.user.UserDto;
import org.novize.api.enums.NotificationType;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private String id;
    private UserDto recipient;
    private NotificationType type;
    private String message;
    private String payload;
    private boolean read;
    private LocalDateTime createdAt;
}
