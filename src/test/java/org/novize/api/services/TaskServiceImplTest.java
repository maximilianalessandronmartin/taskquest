package org.novize.api.services;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.novize.api.dtos.task.CreateTaskDto;
import org.novize.api.dtos.task.TaskDto;
import org.novize.api.dtos.task.UpdateTaskDto;
import org.novize.api.enums.Urgency;
import org.novize.api.exceptions.UserNotFoundException;
import org.novize.api.model.Task;
import org.novize.api.model.User;
import org.novize.api.repository.TaskRepository;
import org.novize.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@ActiveProfiles("test")
public class TaskServiceImplTest {

    @Autowired
    private TaskServiceImpl taskService;

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    public void testCreateTask_Success() {
        // Mock authenticated user
        User mockUser = new User();
        mockUser.setId("test-id");
        mockUser.setFirstname("John");
        mockUser.setLastname("Doe");
        mockUser.setUsername("john.doe@email.com");
        mockUser.setPassword("password123");

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getName()).thenReturn("john.doe@email.com");


        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(mockSecurityContext);

        when(userDetailsService.loadUserByUsername("john.doe@email.com")).thenReturn(mockUser);


        // Prepare task DTO
        CreateTaskDto createTaskDto = CreateTaskDto.builder()
                .name("Test Task")
                .description("This is a test description")
                .dueDate(LocalDateTime.now().plusDays(1))
                .urgency(Urgency.HIGH)
                .build();

        Task mockTask = new Task(createTaskDto.getName(), createTaskDto.getDescription(),
                createTaskDto.getUrgency(), createTaskDto.getDueDate(), mockUser);
        mockTask.setId("generated-id");
        mockTask.setCreatedAt(LocalDateTime.now());
        mockTask.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(mockUser));
        when(userDetailsService.loadUserByUsername("john.doe@email.com")).thenReturn(mockUser);
        when(taskRepository.save(Mockito.any(Task.class))).thenReturn(mockTask);

        // Execute
        TaskDto createdTaskDto = taskService.create(createTaskDto);

        // Validate
        assertNotNull(createdTaskDto);
        assertEquals("generated-id", createdTaskDto.getId());
        assertEquals("Test Task", createdTaskDto.getName());
        assertEquals("This is a test description", createdTaskDto.getDescription());
        assertEquals(Urgency.HIGH, createdTaskDto.getUrgency());

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task savedTask = taskCaptor.getValue();

        assertEquals("Test Task", savedTask.getName());
        assertEquals("This is a test description", savedTask.getDescription());
        assertEquals(Urgency.HIGH, savedTask.getUrgency());
        assertEquals(mockUser, savedTask.getUser());
    }

    @Test
    public void testCreateTask_UserNotFound() {
        // Mock unauthenticated user
        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getName()).thenReturn(null);

        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(mockSecurityContext);

        // Prepare task DTO
        CreateTaskDto createTaskDto = CreateTaskDto.builder()
                .name("Test Task")
                .description("This is a test description")
                .dueDate(LocalDateTime.now().plusDays(1))
                .urgency(Urgency.HIGH)
                .build();

        // Execute and validate
        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskService.create(createTaskDto));
        assertEquals("User not found", exception.getMessage());

        verifyNoInteractions(userDetailsService, taskRepository);
    }

    @Test
    public void testCreateTask_MissingName() {
        // Mock authenticated user
        User mockUser = new User();
        mockUser.setId("test-id");

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getName()).thenReturn("john.doe");

        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(mockSecurityContext);


        // Prepare task DTO with missing name
        CreateTaskDto createTaskDto = CreateTaskDto.builder()
                .description("This is a test description")
                .dueDate(LocalDateTime.now().plusDays(1))
                .urgency(Urgency.HIGH)
                .build();

        // Execute and validate
        assertThrows(Exception.class, () -> taskService.create(createTaskDto));
        verifyNoInteractions(taskRepository);
    }

    @Test
    public void testGetTaskById_Success() {
        String taskId = "test-task-id";
        Task mockTask = new Task();
        mockTask.setId(taskId);
        mockTask.setName("Test Task");
        mockTask.setDescription("Test Description");
        mockTask.setUrgency(Urgency.HIGH);
        mockTask.setDueDate(LocalDateTime.now());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTask));

        TaskDto result = taskService.getById(taskId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals(mockTask.getName(), result.getName());
        assertEquals(mockTask.getDescription(), result.getDescription());
        assertEquals(mockTask.getUrgency(), result.getUrgency());
    }

    @Test
    public void testDeleteTask_Success() {
        // Arrange
        String taskId = "test-task-id";
        Task mockTask = new Task();
        mockTask.setId(taskId);

        doNothing().when(taskRepository).deleteById(taskId);

        // Act
        taskService.deleteById(taskId);

        // Assert
        verify(taskRepository).deleteById(taskId);
    }

    @Test
    public void testUpdateTask_Success() {
        String taskId = "test-task-id";
        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setName("Old Name");
        existingTask.setDescription("Old Description");
        existingTask.setUrgency(Urgency.LOW);

        UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .urgency(Urgency.HIGH)
                .dueDate(LocalDateTime.now())
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        TaskDto result = taskService.update(taskId, updateDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(Urgency.HIGH, result.getUrgency());
    }

    // Hilfsmethoden für die Testdaten-Erstellung
    private User createMockUser(String userId, long xp) {
        User user = new User();
        user.setId(userId);
        user.setXp(xp);
        return user;
    }

    private Task createMockTask(String taskId, String taskName, boolean completed) {
        Task task = new Task();
        task.setId(taskId);
        task.setName(taskName);
        task.setCompleted(completed);
        return task;
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testSetTaskCompleted_TaskNotFound() {
        String taskId = "nonexistent-task-id";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> taskService.toggleCompleted(taskId)
        );

        assertEquals("Task not found with id: " + taskId, exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testSetTaskCompleted_UserNotFound() {
        String taskId = "test-task-id";

        Task mockTask = createMockTask(taskId, "Test Task", false);

        mockTask.setId(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTask));
        when(userDetailsService.loadUserByUsername("testuser")).thenThrow(new UserNotFoundException("User not found"));

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> taskService.toggleCompleted(taskId)
        );

        assertEquals("User not found", exception.getMessage());
        verifyNoInteractions(userRepository);
    }


    @WithMockUser(username = "testuser", roles = {"USER"})
    @Test
    public void testSetTaskCompleted_Success() {

        // Arrange
        String taskId = "test-task-id";
        Task mockTask = createMockTask(taskId, "Test Task", false);
        Task mockCompletedTask = createMockTask(taskId, "Test Task", true);
        User mockUser = createMockUser("testuser", 1000L);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTask));
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(mockUser);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(taskRepository.save(any(Task.class))).thenReturn(mockCompletedTask);

        // Act
        var result = taskService.toggleCompleted(taskId);

        // Assert
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertTrue(result.getCompleted());
        assertEquals("Test Task", result.getName());
        verify(taskRepository).save(any(Task.class));
        verify(userRepository).save(mockUser);
        verify(taskRepository).findById(taskId);
        verify(userDetailsService).loadUserByUsername("testuser");


    }
}
