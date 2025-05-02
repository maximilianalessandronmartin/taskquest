package org.novize.api.services;

import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.novize.api.dtos.*;
import org.novize.api.exceptions.UserNotFoundException;
import org.novize.api.model.Achievement;
import org.novize.api.model.Task;
import org.novize.api.model.User;
import org.novize.api.model.UserAchievement;
import org.novize.api.repository.AchievementRepository;
import org.novize.api.repository.TaskRepository;
import org.novize.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for managing and performing operations related to tasks.
 * This implementation handles various CRUD and synchronization operations for tasks,
 * as well as manages user-related information and achievements associated with tasks.
 */
@Service
public class TaskServiceImpl implements TaskService {
    private static final Logger logger = LogManager.getLogger(TaskServiceImpl.class);
    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserRepository userrepository;

    @Autowired
    AchievementRepository achievementRepository;
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    UserService userService;
    @Autowired
    AchievementService achievementService;


    /**
     * Creates a new task associated with the currently authenticated user.
     *
     * @param taskDto the data transfer object containing details about the task to be created, such as name, description, urgency, and due date
     * @return a TaskDto object representing the newly created task, including its unique ID, name, description, urgency, due date, creation timestamp, and last updated timestamp
     *
     * @throws RuntimeException if the currently authenticated user is not found
     */
    @Override
    public TaskDto create(CreateTaskDto taskDto) {
        User user = getUser();
        if (user == null) throw new RuntimeException("User not found");
        var task = new Task(taskDto.getName(), taskDto.getDescription(), taskDto.getUrgency(), taskDto.getDueDate(), user);
        var newTask = taskRepository.save(task);
        return TaskDto.builder()
                .id(newTask.getId())
                .name(newTask.getName())
                .description(newTask.getDescription())
                .dueDate(newTask.getDueDate())
                .urgency(newTask.getUrgency())
                .createdAt(newTask.getCreatedAt())
                .updatedAt(newTask.getUpdatedAt())
                .build();

    }

    /**
     * Retrieves the currently authenticated user from the security context.
     * The method fetches the user's authentication details, validates the associated username, and loads the user
     * from the user details service. If any step fails (e.g., missing or invalid username, user not found),
     * it logs the error and returns null.
     *
     * @return the authenticated {@link User} object if successfully retrieved; otherwise, returns null
     */
    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if (username == null || username.isEmpty()) {
            logger.error("Username is null or empty");
            return null;
        }

        User user = (User) userDetailsService.loadUserByUsername(username);
        if (user == null) {
            logger.error("User not found in the repository");
            return null;
        }
        return user;
    }


    /**
     * Toggles the "completed" status of a task identified by its unique ID.
     * If the task does not exist, an {@code EntityNotFoundException} is thrown.
     *
     * @param id the unique identifier of the task to be updated
     * @return the updated {@code Task} object with its completion status toggled
     * @throws EntityNotFoundException if no task is found with the provided ID
     */


    @Override
    public Task setCompleted(String id) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        User user = getUser();
        if (optionalTask.isEmpty()) {
            throw new EntityNotFoundException("Task not found with id: " + id);
        }
        Task task = optionalTask.get();
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        // Nur XP vergeben, wenn die Aufgabe von nicht abgeschlossen zu abgeschlossen wechselt
        if (!task.getCompleted()) {
            user.setXp(user.getXp() + 10);
            userrepository.save(user);
        }

        task.setCompleted(!task.getCompleted());
        return taskRepository.save(task);
    }


    /**
     * Updates an existing task with the provided details. The task to be updated is identified
     * by its unique identifier. Only fields that are not null and different from the current values
     * will be updated. If the task is not found, a RuntimeException is thrown.
     *
     * @param id the unique identifier of the task to be updated
     * @param taskDto updateTaskDto object containing the updated task details such as name,
     *                description, due date, urgency, and completion status
     * @return a TaskDto object representing the updated task, including its unique ID,
     *         updated fields, and timestamps
     * @throws RuntimeException if the task with the specified ID is not found
     */
    @Override
    public TaskDto update(String id, UpdateTaskDto taskDto) {
        // TODO: Exception Handling
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));

        // Check if updateDto fields are not null or not same as existing values else change to new values
        if (taskDto.getName() != null && !Objects.equals(task.getName(), taskDto.getName())) {
            task.setName(taskDto.getName());
        }
        if (taskDto.getDescription() != null && !Objects.equals(task.getDescription(), taskDto.getDescription())) {
            task.setDescription(taskDto.getDescription());
        }
        if (taskDto.getDueDate() != null && !Objects.equals(task.getDueDate(), taskDto.getDueDate())) {
            task.setDueDate(taskDto.getDueDate());
        }
        if (taskDto.getUrgency() != null && !Objects.equals(task.getUrgency(), taskDto.getUrgency())) {
            task.setUrgency(taskDto.getUrgency());
        }
        // Check if completed is not null and not same as existing value else change to new value
        if (taskDto.getCompleted() != null && !Objects.equals(task.getCompleted(), taskDto.getCompleted())) {
            task.setCompleted(taskDto.getCompleted());
        }

        var updatedTask = taskRepository.save(task);
        return TaskDto.builder()
                .id(updatedTask.getId())
                .name(updatedTask.getName())
                .description(updatedTask.getDescription())
                .dueDate(updatedTask.getDueDate())
                .urgency(updatedTask.getUrgency())
                .completed(updatedTask.getCompleted())
                .createdAt(updatedTask.getCreatedAt())
                .updatedAt(updatedTask.getUpdatedAt())
                .build();
    }




    /**
     * Retrieves all tasks associated with the currently authenticated user.
     * The tasks are fetched from the repository using the user's unique identifier
     * and sorted by their creation date in ascending order. Each task is then mapped
     * to a {@link TaskDto} object.
     *
     * @return a list of {@link TaskDto} objects representing the tasks associated
     *         with the currently authenticated user
     * @throws RuntimeException if the currently authenticated user is not found
     */
    @Override
    public List<TaskDto> findAllByUserId() {

        User user = getUser();
        if (user == null) throw new RuntimeException("User not found");

        var tasks = taskRepository.findByUserId(user.getId(), Sort.by("createdAt").ascending());
        return tasks.stream().map(task -> {
            TaskDto dto = TaskDto.builder()
                    .id(task.getId())
                    .name(task.getName())
                    .description(task.getDescription())
                    .dueDate(task.getDueDate())
                    .urgency(task.getUrgency())
                    .completed(task.getCompleted())
                    .createdAt(task.getCreatedAt())
                    .updatedAt(task.getUpdatedAt())
                    .build();

            return dto;
        }).collect(Collectors.toList());

    }

    /**
     * Checks if a task exists in the repository by its unique identifier.
     *
     * @param id the unique identifier of the task to check for existence
     * @return true if a task with the given ID exists, false otherwise
     */
    @Override
    public boolean existsById(String id) {

        return taskRepository.existsById(id);
    }

    /**
     * Fetches synchronized user data, including the user details, list of tasks, and achievements,
     * and returns it as a {@link SyncRequestDto}.
     *
     * @param user the {@link User} object for which the data is to be retrieved
     * @return a {@link SyncRequestDto} object containing the user's details, associated tasks,
     *         and mapped user achievements
     */
    @Override
    public SyncRequestDto getData(User user) {
        // Get all tasks
        List<TaskDto> tasks = taskRepository.findByUserId(user.getId(), Sort.by("createdAt").ascending());
        List<UserAchievement> userAchievementList = userService.getUserAchievements(user);

        // Map userAchievementList to UserAchievementDtoList
        List<UserAchievementDto> userAchievementDtoList = userAchievementList.stream()
                .map(ua -> UserAchievementDto.builder()
                        .achievementId(ua.getAchievement().getId())
                        .unlockedAt(ua.getUnlockedAt())
                        .announced(ua.isAnnounced())
                        .build())
                .toList();

        return SyncRequestDto.builder()
                .user(UserDto.builder()
                        .id(user.getId())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .username(user.getUsername())
                        .xp(user.getXp())
                        .build())
                .tasks(tasks)
                .userAchievements(userAchievementDtoList)
                .build();
    }

    @Override
    public SyncRequestDto syncData(User user, SyncRequestDto syncRequestDto) {

        user.setXp(syncRequestDto.getUser().getXp());




        //Get existing tasks
        for (TaskDto taskDto : syncRequestDto.getTasks()) {

            // Check if the task already exists
            boolean taskExists = existsById(taskDto.getId());
            if (!taskExists) {
                // If the task does not exist, create it
                CreateTaskDto createTaskDto = CreateTaskDto.builder()
                        .name(taskDto.getName())
                        .description(taskDto.getDescription())
                        .dueDate(taskDto.getDueDate())
                        .urgency(taskDto.getUrgency())
                        .build();
                create(createTaskDto);
            } else {
                // If the task exists, update it
                TaskDto existingTask = getTaskDtoById(taskDto.getId());
                if (existingTask != null) {
                    UpdateTaskDto updateTaskDto = UpdateTaskDto.builder()
                            .name(taskDto.getName())
                            .description(taskDto.getDescription())
                            .completed(taskDto.getCompleted())
                            .build();
                    update(existingTask.getId(), updateTaskDto);
                }
            }
        }
        //Get all tasks
        List<TaskDto> tasks = taskRepository.findByUserId(user.getId(), Sort.by("createdAt").ascending());
        List<UserAchievement> userAchievementList = userService.getUserAchievements(user);

        for (UserAchievementDto userAchievement : syncRequestDto.getUserAchievements()) {
            boolean achievementExists = userAchievementList.stream().anyMatch(ua -> ua.getAchievement().getId().equals(userAchievement.getAchievementId()));
            if (!achievementExists) {
                // If the achievement does not exist, create it
                // Map the UserAchievementDto to UserAchievement
                Achievement achievement = achievementService.getById(userAchievement.getAchievementId());

                UserAchievement newAchievement = UserAchievement.builder()
                        .announced(userAchievement.isAnnounced())
                        .achievement(achievement)
                        .user(user)
                        .build();
                userAchievementList = userService.addUserAchievement(user, newAchievement);
            } else {
                // Update it
                UserAchievement existingAchievement = userAchievementList.stream().filter(ua -> ua.getId().equals(userAchievement.getId())).findFirst().orElse(null);
                if (existingAchievement != null) {
                    existingAchievement.setAnnounced(userAchievement.isAnnounced());
                    // Save the updated achievement
                    userAchievementList = userService.updateUserAchievements(user, existingAchievement);
                }

            }
        }
        // Map userAchievementList to UserAchievementDtoList
        List<UserAchievementDto> userAchievementDtoList = userAchievementList.stream()
                .map(ua -> UserAchievementDto.builder()
                        .achievementId(ua.getAchievement().getId())
                        .unlockedAt(ua.getUnlockedAt())
                        .announced(ua.isAnnounced())
                        .build())
                .toList();

        return SyncRequestDto.builder()
                .user(UserDto.builder()
                        .id(user.getId())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .username(user.getUsername())
                        .xp(user.getXp())
                        .build())
                .tasks(tasks)
                .userAchievements(userAchievementDtoList)
                .build();

    }



    /**
     * Retrieves a TaskDto object by its unique identifier.
     * This method fetches the task from the repository based on the provided ID,
     * and converts it into a TaskDto object. If no task is found with the given ID,
     * a {@code RuntimeException} is thrown.
     *
     * @param id the unique identifier of the task to retrieve
     * @return a {@code TaskDto} object containing the details of the requested task
     * @throws RuntimeException if no task is found with the provided ID
     */
    @Override
    public TaskDto getTaskDtoById(String id) {

        Optional<Task> optional = taskRepository.findById(id);
        var task = optional.orElse(null);
        if (task == null) {
            throw new RuntimeException("Task not found");
        }
        return TaskDto.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .urgency(task.getUrgency())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }


    /**
     * Searches for tasks based on a provided query string and returns a paginated list of tasks.
     * If no query is provided, all tasks are retrieved in a paginated manner.
     *
     * @param query the search term to filter tasks by their names (case-insensitive). If null or empty, all tasks are returned.
     * @param page the page number to retrieve, starting from 1.
     * @param pageSize the number of tasks to display per page.
     * @return a {@link TaskListDto} containing the tasks matching the search criteria,
     *         along with pagination information such as the total number of pages and count of tasks.
     */
    @Override
    public TaskListDto search(String query, int page, int pageSize) {

        Page<Task> tasks;
        final Pageable request = PageRequest.of(page - 1, pageSize, Sort.by("createdAt"));
        if (query == null || query.isEmpty()) {
            tasks = taskRepository.findAll(request);

            tasks.getTotalPages();
        } else {
            //Search by name return paginated results

            tasks = taskRepository.findByNameContainingIgnoreCase(query, request);
        }

        // Map tasks to TaskDto
        List<TaskDto> taskDtos = tasks.stream().map(task -> {
            TaskDto dto = TaskDto.builder()
                    .id(task.getId())
                    .name(task.getName())
                    .description(task.getDescription())
                    .dueDate(task.getDueDate())
                    .urgency(task.getUrgency())
                    .createdAt(task.getCreatedAt())
                    .updatedAt(task.getUpdatedAt())
                    .build();
            return dto;
        }).collect(Collectors.toList());

        return TaskListDto.builder()
                .tasks(taskDtos)
                .pages(tasks.getTotalPages())
                .count(tasks.getTotalElements())
                .build();

    }

    /**
     * Deletes a task identified by its unique ID from the repository.
     *
     * @param id the unique identifier of the task to be deleted
     */
    @Override
    public void deleteById(String id) {
        taskRepository.deleteById(id);
    }


}
