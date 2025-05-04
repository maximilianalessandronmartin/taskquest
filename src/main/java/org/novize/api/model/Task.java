package org.novize.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.novize.api.enums.TaskVisibility;
import org.novize.api.enums.Urgency;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Table(name = "tasks")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @NotNull
    private String name;

    // Default value is empty string
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
    // Due date is not required
    private LocalDateTime dueDate;

    // Completed boolean is not required
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



    @Builder
    public Task(String name, String description, Urgency urgency, LocalDateTime dueDate, User user) {
        this.name = name;
        this.description = description;
        this.urgency = urgency;
        this.dueDate = dueDate;
        this.user = user;
    }



}
