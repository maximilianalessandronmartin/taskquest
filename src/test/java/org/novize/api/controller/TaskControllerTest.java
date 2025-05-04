package org.novize.api.controller;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.novize.api.dtos.task.TaskDto;
import org.novize.api.enums.Urgency;
import org.novize.api.exceptions.InvalidRequestException;
import org.novize.api.exceptions.UserNotFoundException;
import org.novize.api.model.Task;
import org.novize.api.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void markAsComplete_WhenTaskExists_ShouldReturnUpdatedTask() throws Exception {
        String taskId = "12345";
        Task task = Task.builder()
                .name("Sample Task")
                .description("This is a sample task")
                .dueDate(LocalDateTime.of(2025, 5, 15, 10, 0))
                .urgency(Urgency.HIGH)
                .build();

        TaskDto taskDto = TaskDto.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .urgency(task.getUrgency())
                .completed(task.getCompleted())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();

        // Format the LocalDateTime to match the expected format in JSON
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String formattedDueDate = taskDto.getDueDate().format(formatter);


        Mockito.when(taskService.setCompleted(taskId)).thenReturn(task);

        mockMvc.perform(post("/api/tasks/complete/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(taskDto.getId())))
                .andExpect(jsonPath("$.name", is(taskDto.getName())))
                .andExpect(jsonPath("$.description", is(taskDto.getDescription())))
                .andExpect(jsonPath("$.dueDate", is(formattedDueDate)))
                .andExpect(jsonPath("$.urgency", is(taskDto.getUrgency().toString())))
                .andExpect(jsonPath("$.completed", is(taskDto.getCompleted())));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void markAsComplete_WhenTaskDoesNotExist_ShouldReturnNotFound() throws Exception {
        String taskId = "nonexistent";

        Mockito.when(taskService.setCompleted(taskId)).thenThrow(new EntityNotFoundException("Task not found"));

        mockMvc.perform(post("/api/tasks/complete/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void markAsComplete_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        String taskId = "nonexistent";

        Mockito.when(taskService.setCompleted(taskId)).thenThrow(new UserNotFoundException("Task not found"));

        mockMvc.perform(post("/api/tasks/complete/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void markAsComplete_WhenTaskIdIsInvalid_ShouldReturnBadRequest() throws Exception {
        String invalidTaskId = "   ";
        Mockito.when(taskService.setCompleted(invalidTaskId)).thenThrow(new InvalidRequestException("Task not found"));

        mockMvc.perform(post("/api/tasks/complete/{id}", invalidTaskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}