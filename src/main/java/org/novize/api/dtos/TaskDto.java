package org.novize.api.dtos;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.novize.api.enums.Urgency;

import java.time.LocalDateTime;
import java.util.Date;


@Data
@Builder
public class TaskDto {
    @Null
    private String id;
    @NotNull
    private Date createdAt;
    @NotNull
    private Date updatedAt;
    @NotBlank
    @Max(value = 50, message = "Name must be less than 50 characters")
    private String name;
    @NotNull
    @Max(value = 200, message = "Description must be less than 200 characters")
    private String description;
    @NotNull
    private LocalDateTime dueDate;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Urgency urgency;
    @NotNull
    private Boolean completed;




}
