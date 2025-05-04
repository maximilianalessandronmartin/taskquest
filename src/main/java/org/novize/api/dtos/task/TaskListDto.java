package org.novize.api.dtos.task;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TaskListDto {
    private List<TaskDto> tasks;
    private int pages;
    private Long count;
}
