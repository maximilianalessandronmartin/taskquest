package org.novize.api.controller;

import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.novize.api.dtos.CreateTaskDto;
import org.novize.api.dtos.TaskDto;
import org.novize.api.dtos.TaskListDto;
import org.novize.api.dtos.UpdateTaskDto;
import org.novize.api.model.Task;
import org.novize.api.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("{id}")
    public TaskDto getById(@PathVariable String id) {

        return taskService.getTaskDtoById(id);
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