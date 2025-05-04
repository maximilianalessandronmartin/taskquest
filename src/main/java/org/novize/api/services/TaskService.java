package org.novize.api.services;


import org.novize.api.dtos.task.CreateTaskDto;
import org.novize.api.dtos.task.TaskDto;
import org.novize.api.dtos.task.TaskListDto;
import org.novize.api.dtos.task.UpdateTaskDto;
import org.novize.api.model.Task;
import org.novize.api.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service interface for managing tasks. Provides methods for creating, updating,
 * retrieving, deleting, and managing task data.
 */

@Service
public interface TaskService {

   Task findById(String id);

   TaskDto create(CreateTaskDto taskDto);

   Task setCompleted(String id);

   TaskDto update(String id, UpdateTaskDto taskDto);


   TaskDto getById(String id);

   TaskListDto search(String name, int page, int pageSize);


   void deleteById(String id);


   List<TaskDto> findAllByUserId();

   Task shareTaskwithFriend(String taskId, String friendId, User currentUser);

   Task unshareTask(String id, String username, User currentUser);

   boolean existsById(String id);

   List<Task> getSharedWithMeTasks(User currentUser);
}
