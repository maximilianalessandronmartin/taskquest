package org.novize.api.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Table(name = "user_achievements")
@RequiredArgsConstructor
@Entity
@Getter
@Setter
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @CreationTimestamp
    private LocalDateTime unlockedAt;

    @NotNull
    private boolean unlocked = true;

    @NotNull
    private boolean announced;

    @Builder
    public UserAchievement(User user, Achievement achievement, boolean announced) {
        this.user = user;
        this.achievement = achievement;
        this.announced = announced;
    }
}