package org.novize.api.controller;

import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.novize.api.dtos.task.*;
import org.novize.api.mapper.TaskMapper;
import org.novize.api.model.Task;
import org.novize.api.model.User;
import org.novize.api.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing task operations.
 * This class provides endpoints for creating, updating, retrieving, deleting, and searching tasks.
 */

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final Logger LOGGER = LogManager.getLogger(TaskController.class);

    @Autowired
    TaskService taskService;

    @Autowired
    private TaskMapper taskMapper;

    @GetMapping("{id}")
    public TaskDto getById(@PathVariable String id) {

        return taskService.getById(id);
    }


    /**
     * Endpoint to get all tasks for the authenticated user
     * @return List of TaskDto objects
     */
    @GetMapping
    public List<TaskDto> search() {
        LOGGER.debug("Requesting tasks");

        return taskService.findAllByUserId();
    }


    @PutMapping("/create")
    public TaskDto create(@RequestBody @Valid CreateTaskDto taskDto) {
        return taskService.create(taskDto);
    } 

    @PostMapping("/update/{id}")
    public TaskDto update(@PathVariable String id, @RequestBody @Valid UpdateTaskDto taskDto){
        return taskService.update(id, taskDto);
    }
    @DeleteMapping("/delete/{id}")
    public void deleteById(@PathVariable String id) {
        taskService.deleteById(id);
    }


    @PostMapping("/complete/{id}")
    public TaskDto markAsComplete(@PathVariable String id) {
        Task task = taskService.setCompleted(id);
        return TaskDto.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .urgency(task.getUrgency())
                .completed(task.getCompleted())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }


    @PostMapping("/{id}/share")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskDto> shareTask(
            @PathVariable String id,
            @RequestBody ShareTaskDto shareTaskDto,
            @AuthenticationPrincipal User currentUser) {
        Task task = taskService.shareTaskwithFriend(id, shareTaskDto.getUsername(), currentUser);
        return ResponseEntity.ok(taskMapper.toDto(task, currentUser));
    }

    @DeleteMapping("/{id}/share/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskDto> unshareTask(
            @PathVariable String id,
            @PathVariable String username,
            @AuthenticationPrincipal User currentUser) {
        Task task = taskService.unshareTask(id, username, currentUser);
        return ResponseEntity.ok(taskMapper.toDto(task, currentUser));
    }

    @GetMapping("/shared")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskDto>> getSharedTasks(
            @AuthenticationPrincipal User currentUser) {
        List<Task> tasks = taskService.getSharedWithMeTasks(currentUser);
        List<TaskDto> taskDtos = tasks.stream()
                .map(task -> taskMapper.toDto(task, currentUser))
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskDtos);
    }



    // Endpoint to search for tasks by name with pagination
    @GetMapping("/search")
    public TaskListDto getTaskByName(
            @RequestParam(required = false) String query,
            @RequestParam int page,
            @RequestParam int size
    ) {
        return taskService.search(query, page, size);
    }
}