package org.novize.api.dtos;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Builder
public class AchievementDto {

    @NotNull
    private String name;
    @NotNull
    private String description;
    @NotNull
    private int xpRequired;

}
