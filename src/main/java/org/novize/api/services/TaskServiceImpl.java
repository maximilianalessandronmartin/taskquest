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
        // Timer aktualisieren, falls aktiv
        if (task.getTimerActive() && task.getLastTimerUpdateTimestamp() != null) {
            // Zeit seit der letzten Aktualisierung berechnen
            long elapsedSeconds = java.time.Duration.between(
                    task.getLastTimerUpdateTimestamp(),
                    LocalDateTime.now()
            ).getSeconds();

            // Verbleibende Zeit berechnen und sicherstellen, dass sie nicht negativ wird
            int newRemainingTime = Math.max(0, task.getRemainingTimeSeconds() - (int)elapsedSeconds);

            // Timer anhalten, wenn die Zeit abgelaufen ist
            if (newRemainingTime == 0) {
                task.setTimerActive(false);
            }

            // Werte aktualisieren
            task.setRemainingTimeSeconds(newRemainingTime);
            task.setLastTimerUpdateTimestamp(LocalDateTime.now());

            // Aktualisierte Task speichern
            taskRepository.save(task);
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

        var taskDtos =  tasks.stream()
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
     * Shares a task with a specified friend if the current user owns the task
     * and the friend is in their friend list. The task's visibility is updated
     * to reflect the sharing status, and the friend is added to the task's
     * shared list.
     *
     * @param taskId     the unique identifier of the task to be shared
     * @param friendId   the username of the friend with whom the task is to be shared
     * @param currentUser the {@link User} object representing the currently authenticated user
     * @return the updated {@link Task} object with the sharing information applied
     * @throws EntityNotFoundException if the task with the given ID does not exist
     * @throws AccessDeniedException   if the current user does not own the task
     * @throws UserNotFoundException   if the friend with the given username is not found
     * @throws InvalidRequestException if the specified friend is not in the current user's friend list
     */
    @Override
    public Task shareTaskwithFriend(String taskId, String friendId, User currentUser) {
        Task task = findById(taskId);
        if (task == null) {
            throw new EntityNotFoundException("Task not found with id: " + taskId);
        }

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only share your own tasks");
        }

        User friend = userService.findUserByUsername(friendId);
        if (friend == null) {
            throw new UserNotFoundException("Friend not found with username: " + friendId);
        }

        if (friendshipService.areNotFriends(currentUser, friend)) {
            throw new InvalidRequestException("You can only share tasks with friends");
        }

        task.getSharedWith().add(friend);
        task.setVisibility(TaskVisibility.SHARED);

        notificationService.sendNotification(
                friend,
                NotificationType.TASK_SHARED,
                currentUser.getFirstname() + " " + currentUser.getLastname() + " shares the task " + task.getName() + " with you",
                "{\"senderId\": \"" + currentUser.getId() + "\"}"
        );

        return taskRepository.save(task);
    }



    /**
     * Unshares a task with a specified user. This method removes the specified user
     * from the list of users with whom the task is shared. If there are no more users
     * sharing the task, the task's visibility is reset to PRIVATE.
     *
     * @param taskId the unique identifier of the task to be unshared
     * @param username the username of the user to be removed from the shared list
     * @param currentUser the currently authenticated user who owns the task
     * @return the updated {@link Task} object after unsharing the specified user
     * @throws AccessDeniedException if the currently authenticated user does not own the task
     */
    public Task unshareTask(String taskId, String username, User currentUser) {
        Task task = findById(taskId);

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Sie können nur eigene Tasks nicht mehr teilen");
        }

        User user = userService.findUserByUsername(username);
        task.getSharedWith().remove(user);

        // Wenn keine Benutzer mehr, auf die der Task geteilt wird, setzen wir ihn zurück auf PRIVATE
        if (task.getSharedWith().isEmpty()) {
            task.setVisibility(TaskVisibility.PRIVATE);
        }

        return taskRepository.save(task);
    }

    /**
     * Gibt alle Tasks zurück, die mit dem Benutzer geteilt wurden
     */
    public List<Task> getSharedWithMeTasks(User user) {
        return taskRepository.findSharedWithUser(user);
    }

    @Override
    public List<Task> getOwnedTasks(User currentUser) {
        return taskRepository.findByUserId(currentUser.getId(), Sort.by("createdAt"));
    }

    /**
     * Gibt alle Tasks zurück, die der Benutzer erstellt hat oder die mit ihm geteilt wurden
     */
    public List<Task> getAllTasksForUser(User user) {
        return taskRepository.findTasksForUser(user);
    }

    /**
     * Starts the timer for a specific task. This method activates the timer for the given task,
     * sets default values for the timer if necessary, and updates the task's state accordingly.
     * The user must have access rights to the task to start the timer.
     *
     * @param taskId the unique identifier of the task for which the timer is to be started
     * @param currentUser the user attempting to start the timer, used for authorization checks
     * @return a TaskDto object containing the updated task details after the timer has been started
     * @throws AccessDeniedException if the current user does not have access to the given task
     */
    @Override
    public TaskDto startTimer(String taskId, User currentUser) {
        Task task = findById(taskId);
        if (!task.hasAccess(currentUser)) {
            throw new AccessDeniedException("Sie haben keinen Zugriff auf diese Aufgabe");
        }

        // Standardwert setzen, falls nicht gesetzt
        if (task.getPomodoroTimeSeconds() == null) {
            task.setPomodoroTimeSeconds(25 * 60); // 25 Minuten in Sekunden
        }

        // Falls remainingTimeSeconds nicht gesetzt oder 0 ist, auf pomodoroTimeSeconds zurücksetzen
        if (task.getRemainingTimeSeconds() == null || task.getRemainingTimeSeconds() <= 0) {
            task.setRemainingTimeSeconds(task.getPomodoroTimeSeconds());
        }

        task.setTimerActive(true);
        task.setLastTimerUpdateTimestamp(LocalDateTime.now());

        return taskMapper.toDto(taskRepository.save(task), currentUser);
    }


    /**
     * Pauses the timer for the specified task if the user has access to the task.
     *
     * @param taskId the ID of the task whose timer is to be paused
     * @param currentUser the user attempting to pause the task's timer
     * @return a TaskDto object representing the updated task with the paused timer
     * @throws AccessDeniedException if the user is not the owner or not shared on the task
     */
    @Override
    public TaskDto pauseTimer(String taskId, User currentUser) {
        Task task = findById(taskId);
        if (!task.hasAccess(currentUser)) {
            throw new AccessDeniedException("Sie haben keinen Zugriff auf diese Aufgabe");
        }

        // Berechne verstrichene Zeit und aktualisiere verbleibende Zeit
        LocalDateTime now = LocalDateTime.now();
        if (task.getLastTimerUpdateTimestamp() != null && Boolean.TRUE.equals(task.getTimerActive())) {
            long elapsedSeconds = ChronoUnit.SECONDS.between(task.getLastTimerUpdateTimestamp(), now);

            // Spezialfall: Wenn die verbleibende Zeit 1 Sekunde oder weniger beträgt
            // und der Timer pausiert wird, setzen wir sie auf 0
            if (task.getRemainingTimeSeconds() <= 1) {
                task.setRemainingTimeSeconds(0);
                // Send Notification to user and potentially to shared users
                notificationService.sendNotification(
                        currentUser,
                        NotificationType.TASK_COMPLETED,
                        "The Timer for the Task: " + task.getName() + " completed",
                        "{\"taskId\": \"" + task.getId() + "\"}"
                );
                if (!task.getSharedWith().isEmpty()) {
                    for (User sharedUser : task.getSharedWith()) {
                        notificationService.sendNotification(
                                sharedUser,
                                NotificationType.TASK_COMPLETED,
                                "The Timer for the Task: " + task.getName() + " completed",
                                "{\"taskId\": \"" + task.getId() + "\"}"
                        );
                    }
                }
            } else {
                int newRemainingTime = Math.max(0, task.getRemainingTimeSeconds() - (int)elapsedSeconds);
                task.setRemainingTimeSeconds(newRemainingTime);
            }
        }

        task.setTimerActive(false);

        return taskMapper.toDto(taskRepository.save(task), currentUser);
    }

    /**
     * Resets the timer for a given task. The remaining time of the task is reset
     * to its default Pomodoro duration, and the timer is deactivated.
     *
     * @param taskId the unique identifier of the task
     * @param currentUser the user performing the operation, used to verify access rights
     * @return the updated task information as a TaskDto
     * @throws AccessDeniedException if the current user does not own the task
     *         or does not have shared access to it
     */
    @Override
    public TaskDto resetTimer(String taskId, User currentUser) {
        Task task = findById(taskId);
        if (!task.hasAccess(currentUser)) {
            throw new AccessDeniedException("Sie haben keinen Zugriff auf diese Aufgabe");
        }

        // Standardwert setzen, falls nicht gesetzt
        if (task.getPomodoroTimeSeconds() == null) {
            task.setPomodoroTimeSeconds(25 * 60); // 25 Minuten in Sekunden
        }

        task.setRemainingTimeSeconds(task.getPomodoroTimeSeconds());
        task.setTimerActive(false);

        return taskMapper.toDto(taskRepository.save(task), currentUser);
    }

    /**
     * Updates the timer details of a specific task based on the provided input.
     * The method allows updating the remaining time and toggling the timer's active state.
     * Only the task owner or users the task is shared with are authorized to update the timer.
     *
     * @param taskId the unique identifier of the task whose timer is being updated
     * @param timerUpdateDto an object containing the timer details to be updated,
     *        such as remaining time in minutes and the timer's active state
     * @param currentUser the currently authenticated user requesting the update
     * @return a {@code TaskDto} object representing the updated task with the modified timer details
     * @throws AccessDeniedException if the current user is neither the owner of the task
     *         nor included in the task's shared list
     */
    @Override
    public TaskDto updateTimer(String taskId, TimerUpdateDto timerUpdateDto, User currentUser) {
        Task task = findById(taskId);
        if (!task.hasAccess(currentUser)) {
            throw new AccessDeniedException("Sie haben keinen Zugriff auf diese Aufgabe");
        }

        // Aktualisiere verbleibende Zeit, falls angegeben
        if (timerUpdateDto.getRemainingTimeSeconds() != null) {
            task.setRemainingTimeSeconds(timerUpdateDto.getRemainingTimeSeconds());
        }

        // Aktualisiere Timer-Status, falls angegeben
        if (timerUpdateDto.getTimerActive() != null) {
            // Wenn der Timer bereits aktiv ist und aktiv bleiben soll,
            // aktuelle verstrichene Zeit berücksichtigen
            if (Boolean.TRUE.equals(task.getTimerActive()) && Boolean.TRUE.equals(timerUpdateDto.getTimerActive())) {
                LocalDateTime now = LocalDateTime.now();
                if (task.getLastTimerUpdateTimestamp() != null) {
                    long elapsedSeconds = ChronoUnit.SECONDS.between(task.getLastTimerUpdateTimestamp(), now);
                    int newRemainingTime = Math.max(0, task.getRemainingTimeSeconds() - (int)elapsedSeconds);
                    task.setRemainingTimeSeconds(newRemainingTime);
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
