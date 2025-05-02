package org.novize.api.bootstrap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.novize.api.enums.Urgency;
import org.novize.api.model.Task;
import org.novize.api.model.User;
import org.novize.api.repository.TaskRepository;
import org.novize.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TaskSeeder is a component that seeds tasks for a user when they successfully authenticate.
 * It listens for the AuthenticationSuccessEvent and creates default tasks for the user if they don't already exist.
 */

@Component
public class TaskSeeder implements ApplicationListener<AuthenticationSuccessEvent> {
    private static final Logger logger = LogManager.getLogger(TaskSeeder.class);
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        // Get the authenticated user
        String username = event.getAuthentication().getName();
        if (username == null || username.isEmpty()) {
            logger.error("Username is null or empty");
            return;
        }


        Optional<User> optionalUser = userRepository.findByEmail(username);
        if (optionalUser.isEmpty()) {
            logger.error("User with ID {} not found in the repository", username);
            return;
        }
        // Seed tasks for the authenticated user
        seedTasksForUser(optionalUser.get());
    }

    /**
     * Seeds tasks for the given user.
     * But only if the user has no tasks.
     *
     * @param user The user for whom to seed tasks.
     */
    private void seedTasksForUser(User user) {
        // Check if the user already has tasks
        var currentTasks = taskRepository.findByUserId(user.getId(), Sort.by("createdAt") );
        if (currentTasks != null && !currentTasks.isEmpty()) {
            logger.info("User {} already has tasks. Skipping seeding.", user.getUsername());
            return;
        }
        List<Task> tasks = new ArrayList<>();
        tasks.add(Task.builder().name("Task 1").description("Description 1").urgency(Urgency.HIGH).dueDate(LocalDateTime.now().plusDays(1)).user(user).build());
        tasks.add(Task.builder().name("Task 2").description("Description 2").urgency(Urgency.MEDIUM).dueDate(LocalDateTime.now().plusDays(2)).user(user).build());
        tasks.add(Task.builder().name("Task 3").description("Description 3").urgency(Urgency.LOW).dueDate(LocalDateTime.now().plusDays(3)).user(user).build());

        taskRepository.saveAll(tasks);
    }
}