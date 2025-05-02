package org.novize.api.services;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.novize.api.dtos.CreateTaskDto;
import org.novize.api.dtos.TaskDto;
import org.novize.api.enums.Urgency;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
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
        mockTask.setCreatedAt(new Date());
        mockTask.setUpdatedAt(new Date());

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(mockUser));
        when(userDetailsService.loadUserByUsername("john.doe@email.com")).thenReturn(mockUser);
        when(taskRepository.save(Mockito.any(Task.class))).thenReturn(mockTask);

        // Execute
        TaskDto createdTaskDto = taskService. create(createTaskDto);

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

}