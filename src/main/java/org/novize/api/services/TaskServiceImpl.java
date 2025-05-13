package org.novize.api.services;

import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.novize.api.dtos.task.CreateTaskDto;
import org.novize.api.dtos.task.TaskDto;
import org.novize.api.dtos.task.TaskListDto;
import org.novize.api.dtos.task.UpdateTaskDto;
import org.novize.api.dtos.timer.TimerUpdateDto;
import org.novize.api.enums.NotificationType;
import org.novize.api.enums.Relation;
import org.novize.api.enums.TaskVisibility;
import org.novize.api.exceptions.InvalidRequestException;
import org.novize.api.exceptions.UserNotFoundException;
import org.novize.api.mapper.TaskMapper;
import org.novize.api.model.Task;
import org.novize.api.model.User;
import org.novize.api.repository.TaskRepository;
import org.novize.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    UserRepository userRepository;
    @Autowired
    TaskMapper taskMapper;
    @Autowired
    FriendshipService friendshipService;
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    UserService userService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    TimerNotificationService timerNotificationService;

    @Override
    public Task findById(String id) {
        Optional<Task> optional = taskRepository.findById(id);
        return optional.orElse(null);
    }

    /**
     * Creates a new task associated with the currently authenticated user.
     *
     * @param taskDto the data transfer object containing details about the task to be created, such as name, description, urgency, and due date
     * @return a TaskDto object representing the newly created task, including its unique ID, name, description, urgency, due date, creation timestamp, and last updated timestamp
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
    public Task toggleCompleted(String id) {
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
            userRepository.save(user);
        } else {
            user.setXp(user.getXp() - 10);
            userRepository.save(user);
        }

        task.setCompleted(!task.getCompleted());
        return taskRepository.save(task);
    }


    /**
     * Updates an existing task with the provided details. The task to be updated is identified
     * by its unique identifier. Only fields that are not null and different from the current values
     * will be updated. If the task is not found, a RuntimeException is thrown.
     *
     * @param id      the unique identifier of the task to be updated
     * @param taskDto updateTaskDto object containing the updated task details such as name,
     *                description, due date, urgency, and completion status
     * @return a TaskDto object representing the updated task, including its unique ID,
     * updated fields, and timestamps
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
    @Transactional
    public TaskDto getById(String id) {

        Optional<Task> optional = taskRepository.findById(id);
        var task = optional.orElse(null);
        if (task == null) {
            throw new EntityNotFoundException("Task not found");
        }

        return taskMapper.toDto(task, getUser());

    }


    /**
     * Searches for tasks based on a provided query string and returns a paginated list of tasks.
     * If no query is provided, all tasks are retrieved in a paginated manner.
     *
     * @param query    the search term to filter tasks by their names (case-insensitive). If null or empty, all tasks are returned.
     * @param page     the page number to retrieve, starting from 1.
     * @param pageSize the number of tasks to display per page.
     * @return a {@link TaskListDto} containing the tasks matching the search criteria,
     * along with pagination information such as the total number of pages and count of tasks.
     */
    @Override
    public TaskListDto search(String query, int page, int pageSize, User user) {

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

        var taskDtos = tasks.stream()
                .map(task -> taskMapper.toDto(task, user))
                .collect(Collectors.toList());


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

    /**
     * Manages the sharing of a task with another user. This method finds the task
     * and validates the current user's ownership, identifies the target user to be
     * added or removed, updates the sharing information, and adjusts the task's
     * visibility accordingly.
     *
     * @param taskId      the unique identifier of the task to be shared or unshared
     * @param username    the username of the user with whom the task is being shared or unshared
     * @param currentUser the currently authenticated user who owns the task
     * @param isSharing   a flag indicating whether the task is being shared
     *                    (true) or unshared (false)
     * @return the updated {@link Task} entity after processing the sharing action
     */
    @Override
    public Task manageTaskSharing(String taskId, String username, User currentUser, boolean isSharing) {
        Task task = findAndValidateTask(taskId, currentUser);
        User targetUser = findTargetUser(username);

        updateTaskSharing(task, targetUser, isSharing);
        updateTaskVisibility(task);

        return taskRepository.save(task);
    }

    private Task findAndValidateTask(String taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new InvalidRequestException("Aufgabe mit ID " + taskId + " nicht gefunden"));

        if (task.hasNoAccess(currentUser)) {
            throw new AccessDeniedException("Kein Zugriff auf diese Aufgabe");
        }

        if (!task.isOwner(currentUser)) {
            throw new AccessDeniedException("Nur der Eigentümer kann diese Aufgabe teilen oder das Teilen aufheben");
        }

        return task;
    }

    private User findTargetUser(String username) {
        User targetUser = userService.findUserByUsername(username);
        if (targetUser == null) {
            throw new UserNotFoundException("Benutzer mit Benutzernamen " + username + " nicht gefunden");
        }
        return targetUser;
    }

    private void updateTaskSharing(Task task, User targetUser, boolean isSharing) {
        if (isSharing) {
            task.getSharedWith().add(targetUser);
        } else {
            task.getSharedWith().remove(targetUser);
        }
    }

    private void updateTaskVisibility(Task task) {
        if (task.getSharedWith().isEmpty()) {
            task.setVisibility(TaskVisibility.PRIVATE);
        } else if (task.getVisibility() == TaskVisibility.PRIVATE) {
            task.setVisibility(TaskVisibility.SHARED);
        }
    }


    @Override
    public List<TaskDto> getTasksByUserAndRelation(User currentUser, Relation relation) {
        List<Task> tasks = fetchTasksByType(currentUser, relation);
        return convertTasksToDto(tasks, currentUser);
    }

    private List<Task> fetchTasksByType(User currentUser, Relation relation) {
        if (relation == null) {
            return taskRepository.findTasksForUser(currentUser);
        }

        return switch (relation) {
            case Relation.OWNED -> taskRepository.findByUserId(currentUser.getId(), Sort.by("createdAt"));
            case Relation.SHARED -> taskRepository.findSharedWithUser(currentUser);
            default -> taskRepository.findTasksForUser(currentUser);
        };
    }

    private List<TaskDto> convertTasksToDto(List<Task> tasks, User currentUser) {
        return tasks.stream()
                .map(task -> taskMapper.toDto(task, currentUser))
                .collect(Collectors.toList());
    }


    /**
     * Starts the timer for a specific task. This method activates the timer for the given task,
     * sets default values for the timer if necessary, and updates the task's state accordingly.
     * The user must have access rights to the task to start the timer.
     *
     * @param taskId      the unique identifier of the task for which the timer is to be started
     * @param currentUser the user attempting to start the timer, used for authorization checks
     * @return a TaskDto object containing the updated task details after the timer has been started
     * @throws AccessDeniedException if the current user does not have access to the given task
     */
    @Override
    public TaskDto startTimer(String taskId, User currentUser) {
        Task task = findById(taskId);
        if (task.hasNoAccess(currentUser)) {
            throw new AccessDeniedException("Sie haben keinen Zugriff auf diese Aufgabe");
        }

        // Standardwert setzen, falls nicht gesetzt
        if (task.getPomodoroTimeMillis() == null) {
            task.setPomodoroTimeMillis(25L * 60 * 1000); // 25 Minuten in Millisekunden
        }

        // Falls remainingTimeMillis nicht gesetzt oder 0 ist, auf pomodoroTimeMillis zurücksetzen
        if (task.getRemainingTimeMillis() == null || task.getRemainingTimeMillis() <= 0) {
            task.setRemainingTimeMillis(task.getPomodoroTimeMillis());
        }

        task.setTimerActive(true);
        task.setLastTimerUpdateTimestamp(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);

        // WebSocket-Benachrichtigung senden
        TimerUpdateDto timerUpdateDto = new TimerUpdateDto();
        timerUpdateDto.setRemainingTimeMillis(updatedTask.getRemainingTimeMillis());
        timerUpdateDto.setTimerActive(updatedTask.getTimerActive());
        timerNotificationService.sendTimerUpdate(taskId, timerUpdateDto);

        return taskMapper.toDto(updatedTask, currentUser);
    }


    /**
     * Pauses the timer for the specified task if the user has access to the task.
     *
     * @param taskId      the ID of the task whose timer is to be paused
     * @param currentUser the user attempting to pause the task's timer
     * @return a TaskDto object representing the updated task with the paused timer
     * @throws AccessDeniedException if the user is not the owner or not shared on the task
     */
    @Override
    public TaskDto pauseTimer(String taskId, User currentUser) {
        Task task = findById(taskId);
        if (task.hasNoAccess(currentUser)) {
            throw new AccessDeniedException("Sie haben keinen Zugriff auf diese Aufgabe");
        }

        // Berechne verstrichene Zeit und aktualisiere verbleibende Zeit
        LocalDateTime now = LocalDateTime.now();
        if (task.getLastTimerUpdateTimestamp() != null && Boolean.TRUE.equals(task.getTimerActive())) {
            long elapsedSeconds = ChronoUnit.SECONDS.between(task.getLastTimerUpdateTimestamp(), now);


            long newRemainingTime = Math.max(0, task.getRemainingTimeMillis() - (int) elapsedSeconds);
            task.setRemainingTimeMillis(newRemainingTime);

        }

        task.setTimerActive(false);

        return taskMapper.toDto(taskRepository.save(task), currentUser);
    }

    /**
     * Resets the timer for a given task. The remaining time of the task is reset
     * to its default Pomodoro duration, and the timer is deactivated.
     *
     * @param taskId      the unique identifier of the task
     * @param currentUser the user performing the operation, used to verify access rights
     * @return the updated task information as a TaskDto
     * @throws AccessDeniedException if the current user does not own the task
     *                               or does not have shared access to it
     */
    @Override
    public TaskDto resetTimer(String taskId, User currentUser) {
        Task task = findById(taskId);
        if (task.hasNoAccess(currentUser)) {
            throw new AccessDeniedException("Sie haben keinen Zugriff auf diese Aufgabe");
        }

        // Standardwert setzen, falls nicht gesetzt
        if (task.getPomodoroTimeMillis() == null) {
            task.setPomodoroTimeMillis(25 * 60 * 1000L); // 25 Minuten in Sekunden
        }

        task.setRemainingTimeMillis(task.getPomodoroTimeMillis());
        task.setTimerActive(false);

        Task updatedTask = taskRepository.save(task);

        TimerUpdateDto timerUpdateDto = new TimerUpdateDto();
        timerUpdateDto.setTimerActive(updatedTask.getTimerActive());
        timerUpdateDto.setRemainingTimeMillis(updatedTask.getRemainingTimeMillis());
        timerNotificationService.sendTimerUpdate(taskId, timerUpdateDto);


        return taskMapper.toDto(updatedTask, currentUser);
    }

    /**
     * Updates the timer details of a specific task based on the provided input.
     * The method allows updating the remaining time and toggling the timer's active state.
     * Only the task owner or users the task is shared with are authorized to update the timer.
     *
     * @param taskId         the unique identifier of the task whose timer is being updated
     * @param timerUpdateDto an object containing the timer details to be updated,
     *                       such as remaining time in minutes and the timer's active state
     * @param currentUser    the currently authenticated user requesting the update
     * @return a {@code TaskDto} object representing the updated task with the modified timer details
     * @throws AccessDeniedException if the current user is neither the owner of the task
     *                               nor included in the task's shared list
     */
    @Override
    public TaskDto updateTimer(String taskId, TimerUpdateDto timerUpdateDto, User currentUser) {
        Task task = findById(taskId);
        if (task.hasNoAccess(currentUser)) {
            throw new AccessDeniedException("Sie haben keinen Zugriff auf diese Aufgabe");
        }

        // Aktualisiere verbleibende Zeit, falls angegeben
        if (timerUpdateDto.getRemainingTimeMillis() != null) {
            task.setRemainingTimeMillis(timerUpdateDto.getRemainingTimeMillis());
        }

        // Aktualisiere Timer-Status, falls angegeben
        if (timerUpdateDto.getTimerActive() != null) {
            // Wenn der Timer bereits aktiv ist und aktiv bleiben soll,
            // aktuelle verstrichene Zeit berücksichtigen
            if (Boolean.TRUE.equals(task.getTimerActive()) && timerUpdateDto.getTimerActive()) {
                LocalDateTime now = LocalDateTime.now();
                if (task.getLastTimerUpdateTimestamp() != null) {
                    long elapsedSeconds = ChronoUnit.SECONDS.between(task.getLastTimerUpdateTimestamp(), now);
                    long newRemainingTime = Math.max(0, task.getRemainingTimeMillis() - (int) elapsedSeconds);
                    task.setRemainingTimeMillis(newRemainingTime);
                }
            }

            task.setTimerActive(timerUpdateDto.getTimerActive());

            // Bei Aktivierung Zeitstempel aktualisieren
            if (Boolean.TRUE.equals(timerUpdateDto.getTimerActive())) {
                task.setLastTimerUpdateTimestamp(LocalDateTime.now());
            }
        }

        return taskMapper.toDto(taskRepository.save(task), currentUser);
    }

}
