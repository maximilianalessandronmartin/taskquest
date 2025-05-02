package org.novize.api.services;


import org.novize.api.dtos.*;
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

   TaskDto create(CreateTaskDto taskDto);

   Task setCompleted(String id);

   TaskDto update(String id, UpdateTaskDto taskDto);


   TaskDto getTaskDtoById(String id);

   TaskListDto search(String name, int page, int pageSize);


   void deleteById(String id);


   List<TaskDto> findAllByUserId();

   boolean existsById(String id);

   SyncRequestDto getData(User user);

   SyncRequestDto syncData(User user, SyncRequestDto syncRequestDto);
}
