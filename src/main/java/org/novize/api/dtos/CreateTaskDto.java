package org.novize.api.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.novize.api.enums.Urgency;

import java.time.LocalDateTime;

@Data
@Builder
public class CreateTaskDto {
    @NotNull
    private String name;
    private String description;
    private LocalDateTime dueDate;
    private Urgency urgency;
}
