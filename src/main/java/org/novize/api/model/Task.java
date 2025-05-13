package org.novize.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.novize.api.enums.TaskVisibility;
import org.novize.api.enums.Urgency;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Table(name = "tasks")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Task {

    private static final Long DEFAULT_POMODORO_TIME_MILLIS = 25L * 60 * 1000; // 25 Minuten

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @NotNull
    private String name;

    private String description;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default value is Urgency.LOW

    @Enumerated(EnumType.STRING)
    private Urgency urgency = Urgency.LOW;

    private LocalDateTime dueDate;

    private Boolean completed = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany
    @JoinTable(
            name = "task_shared_users",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> sharedWith = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private TaskVisibility visibility = TaskVisibility.PRIVATE;

    // Pomodoro-Timer
    // Pomodoro-Timer
    private Long pomodoroTimeMillis = DEFAULT_POMODORO_TIME_MILLIS; // 25 Minuten in Millisekunden
    private Long remainingTimeMillis = DEFAULT_POMODORO_TIME_MILLIS; // Verbleibende Zeit in Millisekunden
    private LocalDateTime lastTimerUpdateTimestamp; // Zeitpunkt der letzten Timer-Aktualisierung
    private Boolean timerActive = false; // Zeigt an, ob der Timer aktiv ist





    @Builder
    public Task(String name, String description, Urgency urgency, LocalDateTime dueDate, User user) {
        this.name = name;
        this.description = description;
        this.urgency = urgency != null ? urgency : Urgency.LOW;
        this.dueDate = dueDate;
        this.user = user;
    }


    public boolean isOwner(User user) {
        return user != null && this.user.getId().equals(user.getId());
    }

    public boolean hasNoAccess(User user) {
        return user == null ||
                (!this.user.getId().equals(user.getId()) &&
                        this.sharedWith.stream()
                                .noneMatch(sharedUser -> sharedUser.getId().equals(user.getId())));
    }





}
