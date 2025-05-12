package org.novize.api.mapper;


import lombok.RequiredArgsConstructor;
import org.novize.api.dtos.task.TaskDto;
import org.novize.api.model.Task;
import org.novize.api.model.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskMapper {
    private final UserMapper userMapper;

    public TaskDto toDto(Task task, User currentUser) {
        TaskDto dto = new TaskDto();

        // Grundlegende Eigenschaften
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setDescription(task.getDescription());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setDueDate(task.getDueDate());
        dto.setUrgency(task.getUrgency());
        dto.setCompleted(task.getCompleted());

        // Erweiterte Felder
        dto.setVisibility(task.getVisibility());

        // Benutzer, mit denen geteilt wird
        if (task.getSharedWith() != null) {
            dto.setSharedWith(task.getSharedWith().stream()
                    .map(userMapper::toDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setSharedWith(Collections.emptyList());
        }

        // Prüfen, ob der aktuelle Benutzer der Eigentümer ist
        dto.setOwner(task.isOwner(currentUser));

        // Timer-Status
        dto.setPomodoroTimeMillis(task.getPomodoroTimeMillis());
        dto.setRemainingTimeMillis(task.getRemainingTimeMillis());
        dto.setLastTimerUpdateTimestamp(task.getLastTimerUpdateTimestamp());
        dto.setTimerActive(task.getTimerActive());


        return dto;
    }
}