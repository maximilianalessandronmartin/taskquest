package org.novize.api.dtos;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.util.List;

/**
 * DTO for synchronizing user data with the server.
 * Contains user information, tasksDTOs, and achievementDTOs.
 */

@Data
@Schema
@Builder
public class SyncRequestDto {
    private UserDto user;
    private List<TaskDto> tasks;
    private List<UserAchievementDto> userAchievements;
}
