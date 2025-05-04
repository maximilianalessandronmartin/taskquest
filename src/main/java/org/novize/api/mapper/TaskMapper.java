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
    // Bestehenden TaskMapper erweitern
    public TaskDto toDto(Task task, User currentUser) {
        TaskDto dto = toDto(task, currentUser); // Bestehende Methode aufrufen

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
        dto.setOwner(task.getUser() != null && task.getUser().equals(currentUser));

        return dto;
    }
}
