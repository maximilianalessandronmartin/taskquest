package org.novize.api.model;

import jakarta.persistence.*;
import lombok.Data;
import org.novize.api.enums.NotificationType;

import java.time.LocalDateTime;

@Entity
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private User recipient;

    private NotificationType type; // z.B. "FRIEND_REQUEST", "ACHIEVEMENT_UNLOCKED", "TASK_SHARED"

    private String message;

    @Lob
    private String payload; // JSON mit zusätzlichen Daten

    private boolean read = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}