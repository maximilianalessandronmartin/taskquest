package org.novize.api.dtos;

import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Builder
public class TaskListDto {
    private List<TaskDto> tasks;
    private int pages;
    private Long count;
}
