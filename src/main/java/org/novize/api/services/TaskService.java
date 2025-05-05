package org.novize.api.services;


import org.novize.api.dtos.task.CreateTaskDto;
import org.novize.api.dtos.task.TaskDto;
import org.novize.api.dtos.task.TaskListDto;
import org.novize.api.dtos.task.UpdateTaskDto;
import org.novize.api.dtos.timer.TimerUpdateDto;
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

   Task toggleCompleted(String id);

   TaskDto update(String id, UpdateTaskDto taskDto);


   TaskDto getById(String id);

   TaskListDto search(String name, int page, int pageSize, User user);


   void deleteById(String id);


   Task shareTaskwithFriend(String taskId, String friendId, User currentUser);

   Task unshareTask(String id, String username, User currentUser);

   boolean existsById(String id);

   List<Task> getSharedWithMeTasks(User currentUser);

   List<Task> getOwnedTasks(User currentUser);

   List<Task> getAllTasksForUser(User currentUser);

   TaskDto startTimer(String id, User currentUser);

   TaskDto pauseTimer(String id, User currentUser);

   TaskDto resetTimer(String id, User currentUser);

   TaskDto updateTimer(String id, TimerUpdateDto timerUpdateDto, User currentUser);
}
