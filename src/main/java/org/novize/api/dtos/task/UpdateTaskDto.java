package org.novize.api.dtos.task;

import lombok.Builder;
import lombok.Data;
import org.novize.api.enums.Urgency;

import java.time.LocalDateTime;

@Data
@Builder
public class UpdateTaskDto {
    private String name;
    private String description;
    private LocalDateTime dueDate;
    private Urgency urgency;
    private Boolean completed;
}
