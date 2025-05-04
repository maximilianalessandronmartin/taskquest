package org.novize.api.repository;

import org.novize.api.dtos.task.TaskDto;
import org.novize.api.model.Task;
import org.novize.api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends PagingAndSortingRepository<Task, String>, JpaRepository<Task, String> {

    List<Task> findByNameContainingIgnoreCase(String name);

    Optional<Task> findByName(String name);


    List<TaskDto> findByUserId(String id, Sort sort);

    List<Task> findAllByUserId(String id);

    Page<Task> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.user = :user OR :user MEMBER OF t.sharedWith")
    List<Task> findTasksForUser(@Param("user") User user);

    @Query("SELECT t FROM Task t WHERE :user MEMBER OF t.sharedWith")
    List<Task> findSharedWithUser(@Param("user") User user);


}