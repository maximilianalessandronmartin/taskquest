package org.novize.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Schema
public class UserAchievementDto {
    private String id;
    private Long achievementId;
    private LocalDateTime unlockedAt;
    private boolean announced;

    @Builder
    public UserAchievementDto(Long achievementId, LocalDateTime unlockedAt, boolean announced) {
        this.achievementId = achievementId;
        this.unlockedAt = unlockedAt;
        this.announced = announced;
    }
}
