package org.novize.api.repository;

import org.novize.api.dtos.TaskDto;
import org.novize.api.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
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

}