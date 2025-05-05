package org.novize.api.controller;

import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.novize.api.dtos.task.*;
import org.novize.api.dtos.timer.TimerUpdateDto;
import org.novize.api.mapper.TaskMapper;
import org.novize.api.model.Task;
import org.novize.api.model.User;
import org.novize.api.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
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
    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<List<TaskDto>> getAllTasks(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String type) {

        List<Task> tasks;
        if (type != null) {
            tasks = switch (type) {
                case "owned" -> taskService.getOwnedTasks(currentUser);
                case "shared" -> taskService.getSharedWithMeTasks(currentUser);
                default -> taskService.getAllTasksForUser(currentUser);
            };
        } else {
            tasks = taskService.getAllTasksForUser(currentUser);
        }

        List<TaskDto> taskDtos = tasks.stream()
                .map(task -> taskMapper.toDto(task, currentUser))
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskDtos);
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
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TaskDto toggleComplete(@AuthenticationPrincipal User currentUser, @PathVariable String id) {
        Task task = taskService.toggleCompleted(id);
        return taskMapper.toDto(task, currentUser);
    }


    @PostMapping("/{id}/share")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TaskDto shareTask(
            @PathVariable String id,
            @RequestBody ShareTaskDto shareTaskDto,
            @AuthenticationPrincipal User currentUser) {
        Task task = taskService.shareTaskwithFriend(id, shareTaskDto.getUsername(), currentUser);
        return taskMapper.toDto(task, currentUser);
    }

    @DeleteMapping("/{id}/share/{username}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<TaskDto> unshareTask(
            @PathVariable String id,
            @PathVariable String username,
            @AuthenticationPrincipal User currentUser) {
        Task task = taskService.unshareTask(id, username, currentUser);
        return ResponseEntity.ok(taskMapper.toDto(task, currentUser));
    }

    @GetMapping("/shared")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<List<TaskDto>> getSharedTasks(
            @AuthenticationPrincipal User currentUser) {
        List<Task> tasks = taskService.getSharedWithMeTasks(currentUser);
        List<TaskDto> taskDtos = tasks.stream()
                .map(task -> taskMapper.toDto(task, currentUser))
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskDtos);
    }


    /**
     * Searches for tasks based on a query string and retrieves a paginated list of tasks accessible to the authenticated user.
     *
     * @param currentUser the currently authenticated user making the request
     * @param query an optional search query to filter tasks; can be null to fetch all tasks
     * @param page the zero-based page number of the paginated result set
     * @param size the number of tasks to include on each page
     * @return a TaskListDto object containing a list of tasks, total pages, and task count
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TaskListDto getTaskByName(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String query,
            @RequestParam int page,
            @RequestParam int size
    ) {
        return taskService.search(query, page, size, currentUser);
    }

    /**
     * Starts the timer for a specific task.
     * This method is intended for authenticated users to start the timer
     * associated with a task by its unique identifier.
     *
     * @param currentUser the currently authenticated user initiating the request
     * @param id the unique identifier of the task for which the timer is to be started
     * @return a TaskDto object representing the updated state of the task
     */
    @PostMapping("/{id}/timer/start")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TaskDto startTimer(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String id) {
        return taskService.startTimer(id, currentUser);
    }


    /**
     * Pauses the timer for a specific task.
     * This method allows an authenticated user to pause the timer
     * associated with a task by its unique identifier.
     *
     * @param currentUser the currently authenticated user making the request
     * @param id the unique identifier of the task for which the timer should be paused
     * @return a TaskDto object representing the updated state of the task with the paused timer
     */
    @PostMapping("/{id}/timer/pause")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TaskDto pauseTimer(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String id) {
        return taskService.pauseTimer(id, currentUser);
    }


    /**
     * Resets the timer for a specific task.
     * This method allows an authenticated user to reset the timer associated
     * with a task by its unique identifier.
     *
     * @param currentUser the currently authenticated user initiating the request
     * @param id the unique identifier of the task for which the timer should be reset
     * @return a TaskDto object representing the updated state of the task with the reset timer
     */
    @PostMapping("/{id}/timer/reset")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TaskDto resetTimer(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String id) {
        return taskService.resetTimer(id, currentUser);
    }


    /**
     * Updates the timer for a specific task.
     * This method allows an authenticated user to update the timer details
     * for a task by its unique identifier.
     *
     * @param currentUser the currently authenticated user making the request
     * @param id the unique identifier of the task whose timer is to be updated
     * @param timerUpdateDto the data transfer object containing the new timer details
     * @return a TaskDto object representing the updated state of the task
     */
    @PostMapping("/{id}/timer/update")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TaskDto updateTimer(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String id,
            @RequestBody TimerUpdateDto timerUpdateDto) {
        return taskService.updateTimer(id, timerUpdateDto, currentUser);
    }


}